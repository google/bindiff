// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.project.diff;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.database.CommentsDatabase;
import com.google.security.zynamics.bindiff.database.MatchesDatabase;
import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.io.BinExport2Reader;
import com.google.security.zynamics.bindiff.io.matches.DiffRequestMessage;
import com.google.security.zynamics.bindiff.project.matches.AddressPair;
import com.google.security.zynamics.bindiff.project.matches.AddressTriple;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.project.matches.FunctionDiffMetadata;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstruction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstructionComment;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.IProgressDescription;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import com.google.security.zynamics.zylib.types.common.ICommand;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.SwingUtilities;

public class DiffLoader implements ICommand {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private IProgressDescription descriptionTarget = null;
  private final LinkedHashSet<Diff> diffs;

  public DiffLoader() {
    diffs = null;
  }

  public DiffLoader(LinkedHashSet<Diff> diffs) {
    this.diffs = checkNotNull(diffs);
  }

  private static void setBasicBlockComments(
      RawFlowGraph flowGraph, Map<IAddress, String> basicBlockComments) {
    for (Entry<IAddress, String> comment : basicBlockComments.entrySet()) {
      RawBasicBlock basicBlock = flowGraph.getBasicBlock(comment.getKey());
      basicBlock.setComment(comment.getValue());
    }
  }

  private static void setInstructionComment(
      RawFlowGraph flowGraph, Map<Pair<IAddress, ECommentPlacement>, String> dbComments) {
    for (RawBasicBlock rawBasicblock : flowGraph.getNodes()) {
      for (Entry<IAddress, RawInstruction> rawInstruction :
          rawBasicblock.getInstructions().entrySet()) {
        IAddress rawInstructionAddr = rawInstruction.getKey();

        if (rawInstruction.getValue().getComments() != null) {
          // Note: rawInstructionComments size's maximum is 2!
          for (RawInstructionComment rawInstructionComment :
              rawInstruction.getValue().getComments()) {
            Pair<IAddress, ECommentPlacement> dbCommentKey =
                Pair.make(rawInstructionAddr, rawInstructionComment.getPlacement());
            String dbComment = dbComments.get(dbCommentKey);

            if (dbComment != null) {
              String rawCommentText = rawInstructionComment.getText();
              if (!dbComment.equals(rawCommentText)) {
                if (dbComment.indexOf(rawCommentText) != 0) {
                  rawCommentText += " // " + dbComment;
                }
              }

              rawCommentText = dbComment;

              rawInstructionComment.setText(rawCommentText);
            }
          }
        } else {
          var dbAboveCommentKey = Pair.make(rawInstructionAddr, ECommentPlacement.ABOVE_LINE);
          var dbBehindCommentKey = Pair.make(rawInstructionAddr, ECommentPlacement.BEHIND_LINE);

          String aboveComment = dbComments.get(dbAboveCommentKey);
          String behindComment = dbComments.get(dbBehindCommentKey);

          if (aboveComment != null) {
            rawInstruction.getValue().setComment(aboveComment, ECommentPlacement.ABOVE_LINE);
          }
          if (behindComment != null) {
            rawInstruction.getValue().setComment(behindComment, ECommentPlacement.BEHIND_LINE);
          }
        }
      }
    }
  }

  public static RawFlowGraph loadRawFlowGraph(
      CommentsDatabase database, Diff diff, IAddress functionAddr, ESide side)
      throws IOException, SQLException {
    var primaryExportFileReader =
        new BinExport2Reader(diff.getExportFile(ESide.PRIMARY), ESide.PRIMARY);
    var secondaryExportFileReader =
        new BinExport2Reader(diff.getExportFile(ESide.SECONDARY), ESide.SECONDARY);

    RawFunction function = diff.getCallGraph(side).getFunction(functionAddr);

    if (function == null) {
      return null;
    }

    if (function.getFunctionType() == EFunctionType.IMPORTED) {
      return new RawFlowGraph(
          functionAddr,
          function.getName(),
          function.getFunctionType(),
          new ArrayList<>(),
          new ArrayList<>(),
          side);
    }

    RawFlowGraph flowGraph =
        side == ESide.PRIMARY
            ? primaryExportFileReader.readFlowGraph(diff, functionAddr)
            : secondaryExportFileReader.readFlowGraph(diff, functionAddr);

    String imageHash = diff.getDiffMetaData().getImageHash(side);

    if (database != null) {
      Map<Pair<IAddress, ECommentPlacement>, String> instructionComments =
          database.readInstructionComments(imageHash, functionAddr);
      Map<IAddress, String> basicBlockComments =
          database.readBasicblockComments(imageHash, functionAddr);

      setBasicBlockComments(flowGraph, basicBlockComments);
      setInstructionComment(flowGraph, instructionComments);
    }

    return flowGraph;
  }

  public static DiffMetadata preloadDiffMatches(File matchesDatabase) throws SQLException {
    try (var database = new MatchesDatabase(matchesDatabase)) {
      return database.loadDiffMetadata(matchesDatabase);
    }
  }

  void loadDiff(Diff diff, DiffRequestMessage data) throws IOException, SQLException {
    if (diff.isLoaded()) {
      logger.atInfo().log("Diff is already loaded");
      return;
    }

    if (descriptionTarget != null) {
      descriptionTarget.setDescription(String.format("Loading Diff '%s'", diff.getDiffName()));
    }
    logger.atInfo().log("Loading Diff '%s'", diff.getDiffName());

    setSubDescription("Loading function matches...");
    loadDiffMatches(diff);

    setSubDescription("Loading exports...");
    loadRawCallGraphs(diff, data);

    setSubDescription("Loading function matches...");
    setFunctionMatches(diff);

    setSubDescription("Loading call matches...");
    setCallMatches(diff);

    setChangedFunctionsCount(diff);

    diff.setLoaded(true);

    setSubDescription("Preparing UI...");

    // GUI components are created and/or updated here. This must happen
    // from the outside of the progress thread, or at least must be called
    // by invokeLater!
    SwingUtilities.invokeLater(
        () -> {
          for (DiffListener listener : diff.getListener()) {
            listener.loadedDiff(diff);
          }
        });
  }

  private void loadDiffMatches(Diff diff) throws IOException, SQLException {
    File matchesDatabase = diff.getMatchesDatabase();
    if (!matchesDatabase.exists()) {
      throw new IOException(String.format("Couldn't find '%s'.", matchesDatabase.getPath()));
    }

    logger.atInfo().log(" - Loading Diff '%s'", matchesDatabase.getPath());

    try (var database = new MatchesDatabase(matchesDatabase)) {
      diff.setMatches(database.loadFunctionMatches(diff));
    }
  }

  private void loadRawCallGraphs(Diff diff, DiffRequestMessage data)
      throws IOException, SQLException {
    // TODO(cblichmann): Load these in parallel using an ExecutorService.
    var primaryReader = new BinExport2Reader(diff.getExportFile(ESide.PRIMARY), ESide.PRIMARY);
    var secondaryReader =
        new BinExport2Reader(diff.getExportFile(ESide.SECONDARY), ESide.SECONDARY);

    RawCallGraph primaryCallGraph;
    RawCallGraph secondaryCallGraph;
    if (!diff.isFunctionDiff()) {
      // If requested, read full call graph
      setSubDescription("Reading raw primary call graph...");
      primaryCallGraph = primaryReader.readCallGraph();
      setSubDescription("Reading raw secondary call graph...");
      secondaryCallGraph = secondaryReader.readCallGraph();
    } else if (data != null) {
      // Visual diff from the disassembler, load reduced call graph
      setSubDescription("Reading raw primary call graph...");
      primaryCallGraph = primaryReader.readSingleFunctionDiffCallGraph(data);
      setSubDescription("Reading raw secondary call graph...");
      secondaryCallGraph = secondaryReader.readSingleFunctionDiffCallGraph(data);
    } else {
      // This gets called for saved "Single Function Diff Views" only.
      FunctionDiffMetadata metadata = (FunctionDiffMetadata) diff.getMetadata();
      String priFunctionName = metadata.getFunctionName(ESide.PRIMARY);
      EFunctionType priFunctionType = metadata.getFunctionType(ESide.PRIMARY);

      IAddress priFunctionAddr =
          diff.getMatches().getFunctionMatches()[0].getIAddress(ESide.PRIMARY);
      var priFunction =
          new RawFunction(priFunctionAddr, priFunctionName, priFunctionType, ESide.PRIMARY);

      List<RawFunction> priFunctionList = new ArrayList<>();
      priFunctionList.add(priFunction);

      primaryCallGraph = new RawCallGraph(priFunctionList, new ArrayList<>(), ESide.PRIMARY);
      String secFunctionName = metadata.getFunctionName(ESide.SECONDARY);
      EFunctionType secFunctionType = metadata.getFunctionType(ESide.SECONDARY);

      IAddress secFunctionAddr =
          diff.getMatches().getFunctionMatches()[0].getIAddress(ESide.SECONDARY);
      RawFunction secFunction =
          new RawFunction(secFunctionAddr, secFunctionName, secFunctionType, ESide.SECONDARY);

      List<RawFunction> secFunctionList = new ArrayList<>();
      secFunctionList.add(secFunction);

      secondaryCallGraph = new RawCallGraph(secFunctionList, new ArrayList<>(), ESide.SECONDARY);

      try (var matchesDatabase = new MatchesDatabase(diff.getMatchesDatabase())) {
        matchesDatabase.setFunctionDiffCounts(priFunction, secFunction);
      }
    }

    diff.setCallGraph(primaryCallGraph, ESide.PRIMARY);
    diff.setCallGraph(secondaryCallGraph, ESide.SECONDARY);

    if (!diff.isFunctionDiff()) {
      setSubDescription("Preprocessing raw primary flow graphs...");
      primaryReader.readFlowGraphsStatistics(diff);

      setSubDescription("Preprocessing raw secondary flow graphs...");
      secondaryReader.readFlowGraphsStatistics(diff);
    }
  }

  private void setCallMatches(Diff diff) throws IOException, SQLException {
    File matchesDatabase = diff.getMatchesDatabase();
    if (!matchesDatabase.exists()) {
      throw new IOException(String.format("Couldn't find '%s'.", matchesDatabase.getPath()));
    }

    try (var database = new MatchesDatabase(matchesDatabase)) {
      int matchedCounter = 0;

      Map<AddressPair, AddressPair> priToSecDbCallMatchResult =
          database.loadMatchedCallAddresses(diff);

      // create call triple addresses to call map
      ImmutableMap<AddressTriple, RawCall> secCallAddrTripleToSecCall =
          Maps.uniqueIndex(
              diff.getCallGraph(ESide.SECONDARY).getEdges(),
              input ->
                  new AddressTriple(
                      input.getSource().getAddress().toLong(),
                      input.getTarget().getAddress().toLong(),
                      input.getSourceInstructionAddr().toLong()));

      // set identical matched jumps (primary and secondary source functions, source call
      // instructions and target function are matched to each other.
      for (RawCall priCall : diff.getCallGraph(ESide.PRIMARY).getEdges()) {
        RawFunction sourceFunction = priCall.getSource();
        RawFunction targetFunction = priCall.getTarget();

        long priSource = sourceFunction.getAddress().toLong();
        long priCallAddr = priCall.getSourceInstructionAddr().toLong();

        if (sourceFunction.getMatchedFunction() != null
            && targetFunction.getMatchedFunction() != null) {
          long secSource = sourceFunction.getMatchedFunction().getAddress().toLong();
          long secTarget = targetFunction.getMatchedFunction().getAddress().toLong();

          var priDbCallMatchResult = new AddressPair(priSource, priCallAddr);
          AddressPair secDbCallMatchResult = priToSecDbCallMatchResult.get(priDbCallMatchResult);

          if (secDbCallMatchResult != null) {
            long secCallAddr = secDbCallMatchResult.getAddress(ESide.SECONDARY);
            var secCallAddrTriple = new AddressTriple(secSource, secTarget, secCallAddr);

            RawCall secCall = secCallAddrTripleToSecCall.get(secCallAddrTriple);

            if (secCall != null) {
              priCall.setMatchState(true, secCall);
              secCall.setMatchState(true, priCall);

              ++matchedCounter;
            }
          }
        }
      }

      // Create call address pair to target function stack map (only of the not identical matched
      // calls).
      Map<AddressPair, ArrayDeque<RawCall>> secCallAddrPairToCalls = new HashMap<>();
      for (RawCall secCall : diff.getCallGraph(ESide.SECONDARY).getEdges()) {
        if (secCall.getMatchedCall() == null) {
          long secSource = secCall.getSource().getAddress().toLong();
          long secCallAddr = secCall.getSourceInstructionAddr().toLong();

          var secCallAddrPair = new AddressPair(secSource, secCallAddr);
          ArrayDeque<RawCall> callsStack =
              secCallAddrPairToCalls.computeIfAbsent(secCallAddrPair, k -> new ArrayDeque<>());

          callsStack.push(secCall);
        }
      }

      int changedCounter = 0;
      for (RawCall priCall : diff.getCallGraph(ESide.PRIMARY).getEdges()) {
        if (priCall.getMatchedCall() == null) {
          long priSource = priCall.getSource().getAddress().toLong();
          long priCallAddr = priCall.getSourceInstructionAddr().toLong();

          if (priCall.getSource().getMatchedFunction() != null) {
            var priDbCallMatchResult = new AddressPair(priSource, priCallAddr);
            AddressPair secDbCallMatchResult = priToSecDbCallMatchResult.get(priDbCallMatchResult);

            if (secDbCallMatchResult != null) {
              ArrayDeque<RawCall> secCallsStack = secCallAddrPairToCalls.get(secDbCallMatchResult);
              if (secCallsStack != null && secCallsStack.size() > 0) {
                RawCall secCall = secCallsStack.pop();
                priCall.setMatchState(false, secCall);
                secCall.setMatchState(false, priCall);
                ++changedCounter;
              }
            }
          }
        }
      }

      diff.getMatches().setSizeOfMatchedCalls(matchedCounter + changedCounter);
      diff.getMatches().setSizeOfChangedCalls(changedCounter);
    }
  }

  private void setChangedFunctionsCount(Diff diff) {
    int count = 0;
    for (RawFunction function : diff.getCallGraph(ESide.PRIMARY).getNodes()) {
      if (function.isChanged()) {
        ++count;
      }
    }

    diff.getMatches().setSizeOfChangedFunctions(count);
  }

  private void setFunctionMatches(Diff diff) {
    RawCallGraph priCallGraph = diff.getCallGraph(ESide.PRIMARY);
    RawCallGraph secCallGraph = diff.getCallGraph(ESide.SECONDARY);

    for (FunctionMatchData functionMatch : diff.getMatches().getFunctionMatches()) {
      RawFunction priFunction = priCallGraph.getFunction(functionMatch.getIAddress(ESide.PRIMARY));
      RawFunction secFunction =
          secCallGraph.getFunction(functionMatch.getIAddress(ESide.SECONDARY));

      if (priFunction != null && secFunction != null) {
        priFunction.setMatch(secFunction, functionMatch);
        secFunction.setMatch(priFunction, functionMatch);
      }
    }
  }

  private void setSubDescription(String description) {
    if (descriptionTarget != null) {
      descriptionTarget.setSubDescription(description);
    }
  }

  @Override
  public void execute() throws Exception {
    for (Diff diff : diffs) {
      loadDiff(diff, null);
    }
  }

  public void setProgressDescriptionTarget(IProgressDescription descriptionTarget) {
    this.descriptionTarget = descriptionTarget;
  }
}
