package com.google.security.zynamics.bindiff.project.diff;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.security.zynamics.bindiff.database.CommentsDatabase;
import com.google.security.zynamics.bindiff.database.MatchesDatabase;
import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.io.BinExport2Reader;
import com.google.security.zynamics.bindiff.io.matches.FunctionDiffSocketXmlData;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.matches.AddressPair;
import com.google.security.zynamics.bindiff.project.matches.AddressTriple;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.project.matches.FunctionDiffMetaData;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import javax.swing.SwingUtilities;

public class DiffLoader implements ICommand {
  private IProgressDescription descriptionTarget = null;
  private final LinkedHashSet<Diff> diffs;

  public DiffLoader() {
    diffs = null;
  }

  public DiffLoader(final LinkedHashSet<Diff> diffs) {
    this.diffs = Preconditions.checkNotNull(diffs);
  }

  private static void setBasicblockComments(
      final RawFlowGraph flowgraph, final Map<IAddress, String> basicblockComments) {
    for (final Entry<IAddress, String> comment : basicblockComments.entrySet()) {
      final RawBasicBlock basicblock = flowgraph.getBasicblock(comment.getKey());
      basicblock.setComment(comment.getValue());
    }
  }

  private static void setInstructionComment(
      final RawFlowGraph flowgraph,
      final Map<Pair<IAddress, ECommentPlacement>, String> dbComments) {
    for (final RawBasicBlock rawBasicblock : flowgraph.getNodes()) {
      for (final Entry<IAddress, RawInstruction> rawInstruction :
          rawBasicblock.getInstructions().entrySet()) {
        final IAddress rawInstructionAddr = rawInstruction.getKey();

        if (rawInstruction.getValue().getComments() != null) {
          // Note: rawInstructionComments size's maximum is 2!
          for (final RawInstructionComment rawInstructionComment :
              rawInstruction.getValue().getComments()) {
            final Pair<IAddress, ECommentPlacement> dbCommentKey =
                Pair.make(rawInstructionAddr, rawInstructionComment.getPlacement());
            final String dbComment = dbComments.get(dbCommentKey);

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
          final Pair<IAddress, ECommentPlacement> dbAboveCommentKey =
              Pair.make(rawInstructionAddr, ECommentPlacement.ABOVE_LINE);
          final Pair<IAddress, ECommentPlacement> dbBehindCommentKey =
              Pair.make(rawInstructionAddr, ECommentPlacement.BEHIND_LINE);

          final String aboveComment = dbComments.get(dbAboveCommentKey);
          final String behindComment = dbComments.get(dbBehindCommentKey);

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

  public static RawFlowGraph loadRawFlowgraph(
      final CommentsDatabase database,
      final Diff diff,
      final IAddress functionAddr,
      final ESide side)
      throws IOException, SQLException {
    final BinExport2Reader primaryExportFileReader =
        new BinExport2Reader(diff.getExportFile(ESide.PRIMARY), ESide.PRIMARY);
    final BinExport2Reader secondaryExportFileReader =
        new BinExport2Reader(diff.getExportFile(ESide.SECONDARY), ESide.SECONDARY);

    final RawFunction function = diff.getCallgraph(side).getFunction(functionAddr);

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

    final RawFlowGraph flowgraph =
        side == ESide.PRIMARY
            ? primaryExportFileReader.readFlowgraph(diff, functionAddr)
            : secondaryExportFileReader.readFlowgraph(diff, functionAddr);

    final String imageHash = diff.getDiffMetaData().getImageHash(side);

    if (database != null) {
      final Map<Pair<IAddress, ECommentPlacement>, String> instructionComments =
          database.readInstructionComments(imageHash, functionAddr);
      final Map<IAddress, String> basicblockComments =
          database.readBasicblockComments(imageHash, functionAddr);

      setBasicblockComments(flowgraph, basicblockComments);
      setInstructionComment(flowgraph, instructionComments);
    }

    return flowgraph;
  }

  public static DiffMetaData preloadDiffMatches(final File matchesDatabase) throws SQLException {
    try (final MatchesDatabase database = new MatchesDatabase(matchesDatabase)) {
      return database.loadDiffMetaData(matchesDatabase);
    }
  }

  private void loadDiff(final Diff diff) throws IOException, SQLException {
    if (diff.isLoaded()) {
      Logger.logInfo("Diff is already loaded!");
      return;
    }

    descriptionTarget.setDescription(String.format("Loading Diff '%s'", diff.getDiffName()));
    Logger.logInfo("Loading Diff '%s'", diff.getDiffName());
    validateInputFiles(diff);

    try {
      setSubDescription("Loading function matches...");
      loadDiffMatches(diff);

      loadRawCallgraphs(diff);

      setSubDescription("Setting function matches...");
      setFunctionMatches(diff);

      setSubDescription("Loading call matches...");
      setCallMatches(diff);

      setChangedFunctionsCount(diff);

      diff.setLoaded(true);
    } catch (final SQLException e) {
      Logger.logException(e);
      throw e;
    }

    setSubDescription("Preparing UI...");

    // GUI components are created and/or updated here. This must happen
    // from the outside of the progress thread, or at least must be called
    // by invokeLater!
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            for (final IDiffListener listener : diff.getListener()) {
              listener.loadedDiff(diff);
            }
          }
        });
  }

  private void loadDiffMatches(final Diff diff) throws IOException, SQLException {
    final File matchesDatabase = diff.getMatchesDatabase();

    if (!matchesDatabase.exists()) {
      throw new IOException(
          String.format(
              "Couldn't find '%s%s%s'.",
              matchesDatabase.getPath(), File.separator, matchesDatabase.getName()));
    }

    Logger.logInfo(" - Loading Diff '" + matchesDatabase.getPath() + "'");

    try (final MatchesDatabase database = new MatchesDatabase(matchesDatabase)) {
      diff.setMatches(database.loadFunctionMatches(diff));
    }
  }

  private void loadRawCallgraphs(final Diff diff) throws IOException, SQLException, SQLException {
    final BinExport2Reader primaryExportFileReader =
        new BinExport2Reader(diff.getExportFile(ESide.PRIMARY), ESide.PRIMARY);
    final BinExport2Reader secondaryExportFileReader =
        new BinExport2Reader(diff.getExportFile(ESide.SECONDARY), ESide.SECONDARY);

    RawCallGraph primaryCallgraph;
    RawCallGraph secondaryCallgraph;

    if (!diff.isFunctionDiff()) {
      setSubDescription("Read raw primary callgraph...");
      primaryCallgraph = primaryExportFileReader.readCallgraph();
      setSubDescription("Read raw secondary callgraph...");
      secondaryCallgraph = secondaryExportFileReader.readCallgraph();
    } else {
      final FunctionDiffMetaData metaData = (FunctionDiffMetaData) diff.getMetaData();
      final String priFunctionName = metaData.getFunctionName(ESide.PRIMARY);
      final EFunctionType priFunctionType = metaData.getFunctionType(ESide.PRIMARY);

      final IAddress priFunctionAddr =
          diff.getMatches().getFunctionMatches()[0].getIAddress(ESide.PRIMARY);
      final RawFunction priFunction =
          new RawFunction(priFunctionAddr, priFunctionName, priFunctionType, ESide.PRIMARY);

      final List<RawFunction> priFunctionList = new ArrayList<>();
      priFunctionList.add(priFunction);

      primaryCallgraph = new RawCallGraph(priFunctionList, new ArrayList<>(), ESide.PRIMARY);
      final String secFunctionName = metaData.getFunctionName(ESide.SECONDARY);
      final EFunctionType secFunctionType = metaData.getFunctionType(ESide.SECONDARY);

      final IAddress secFunctionAddr =
          diff.getMatches().getFunctionMatches()[0].getIAddress(ESide.SECONDARY);
      final RawFunction secFunction =
          new RawFunction(secFunctionAddr, secFunctionName, secFunctionType, ESide.SECONDARY);

      final List<RawFunction> secFunctionList = new ArrayList<>();
      secFunctionList.add(secFunction);

      secondaryCallgraph = new RawCallGraph(secFunctionList, new ArrayList<>(), ESide.SECONDARY);

      try (final MatchesDatabase matchesDatabase = new MatchesDatabase(diff.getMatchesDatabase())) {
        matchesDatabase.setFunctionDiffCounts(priFunction, secFunction);
      }
    }

    diff.setCallgraph(primaryCallgraph, ESide.PRIMARY);
    diff.setCallgraph(secondaryCallgraph, ESide.SECONDARY);

    if (!diff.isFunctionDiff()) {
      setSubDescription("Preprocessing raw primary flowgraphs...");
      primaryExportFileReader.readFlowGraphsStatistics(diff);

      setSubDescription("Preprocessing raw secondary flowgraphs...");
      secondaryExportFileReader.readFlowGraphsStatistics(diff);
    }
  }

  private void loadSingleFunctionDiffRawCallgraphs(
      final Diff diff, final FunctionDiffSocketXmlData data) throws IOException {
    final BinExport2Reader primaryExportFileReader =
        new BinExport2Reader(diff.getExportFile(ESide.PRIMARY), ESide.PRIMARY);
    final BinExport2Reader secondaryExportFileReader =
        new BinExport2Reader(diff.getExportFile(ESide.SECONDARY), ESide.SECONDARY);

    setSubDescription("Read raw primary callgraph...");
    RawCallGraph primaryCallgraph = primaryExportFileReader.readSingleFunctionDiffCallGraph(data);
    diff.setCallgraph(primaryCallgraph, ESide.PRIMARY);

    setSubDescription("Read raw secondary callgraph...");
    RawCallGraph secondaryCallgraph =
        secondaryExportFileReader.readSingleFunctionDiffCallGraph(data);
    diff.setCallgraph(secondaryCallgraph, ESide.SECONDARY);
  }

  private void setCallMatches(final Diff diff) throws IOException, SQLException {
    final File matchesDatabase = diff.getMatchesDatabase();
    if (!matchesDatabase.exists()) {
      throw new IOException(
          String.format(
              "Couldn't find '%s%s%s'.",
              matchesDatabase.getPath(), File.separator, matchesDatabase.getName()));
    }

    try (final MatchesDatabase database = new MatchesDatabase(matchesDatabase)) {
      int matchedCounter = 0;

      final Map<AddressPair, AddressPair> priToSecDbCallMatchResult =
          database.loadMatchedCallAddresses(diff);

      // create call triple addresses to call map
      final ImmutableMap<AddressTriple, RawCall> secCallAddrTripleToSecCall =
          Maps.uniqueIndex(
              diff.getCallgraph(ESide.SECONDARY).getEdges(),
              new Function<RawCall, AddressTriple>() {
                @Override
                public AddressTriple apply(final RawCall input) {
                  return new AddressTriple(
                      input.getSource().getAddress().toLong(),
                      input.getTarget().getAddress().toLong(),
                      input.getSourceInstructionAddr().toLong());
                }
              });

      // set identical matched jumps (primary and secondary source functions, source call
      // instructions and target function are matched to each other.
      for (final RawCall priCall : diff.getCallgraph(ESide.PRIMARY).getEdges()) {
        final RawFunction sourceFunction = priCall.getSource();
        final RawFunction targetFunction = priCall.getTarget();

        final long priSource = sourceFunction.getAddress().toLong();
        final long priCallAddr = priCall.getSourceInstructionAddr().toLong();

        if (sourceFunction.getMatchedFunction() != null
            && targetFunction.getMatchedFunction() != null) {
          final long secSource = sourceFunction.getMatchedFunction().getAddress().toLong();
          final long secTarget = targetFunction.getMatchedFunction().getAddress().toLong();

          final AddressPair priDbCallMatchResult = new AddressPair(priSource, priCallAddr);
          final AddressPair secDbCallMatchResult =
              priToSecDbCallMatchResult.get(priDbCallMatchResult);

          if (secDbCallMatchResult != null) {
            final long secCallAddr = secDbCallMatchResult.getAddress(ESide.SECONDARY);
            final AddressTriple secCallAddrTriple =
                new AddressTriple(secSource, secTarget, secCallAddr);

            final RawCall secCall = secCallAddrTripleToSecCall.get(secCallAddrTriple);

            if (secCall != null) {
              priCall.setMatchState(true, secCall);
              secCall.setMatchState(true, priCall);

              ++matchedCounter;
            }
          }
        }
      }

      // create call address pair to target function stack map (only of the not identical matched
      // calls)
      final Map<AddressPair, Stack<RawCall>> secCallAddrPairToCalls = new HashMap<>();
      for (final RawCall secCall : diff.getCallgraph(ESide.SECONDARY).getEdges()) {
        if (secCall.getMatchedCall() == null) {
          final long secSource = secCall.getSource().getAddress().toLong();
          final long secCallAddr = secCall.getSourceInstructionAddr().toLong();

          final AddressPair secCallAddrPair = new AddressPair(secSource, secCallAddr);
          Stack<RawCall> callsStack = secCallAddrPairToCalls.get(secCallAddrPair);

          if (callsStack == null) {
            callsStack = new Stack<>();
            secCallAddrPairToCalls.put(secCallAddrPair, callsStack);
          }

          callsStack.push(secCall);
        }
      }

      int changedCounter = 0;
      for (final RawCall priCall : diff.getCallgraph(ESide.PRIMARY).getEdges()) {
        if (priCall.getMatchedCall() == null) {
          final long priSource = priCall.getSource().getAddress().toLong();
          final long priCallAddr = priCall.getSourceInstructionAddr().toLong();

          if (priCall.getSource().getMatchedFunction() != null) {
            final AddressPair priDbCallMatchResult = new AddressPair(priSource, priCallAddr);
            final AddressPair secDbCallMatchResult =
                priToSecDbCallMatchResult.get(priDbCallMatchResult);

            if (secDbCallMatchResult != null) {
              final Stack<RawCall> secCallsStack;
              secCallsStack = secCallAddrPairToCalls.get(secDbCallMatchResult);
              if (secCallsStack != null && secCallsStack.size() > 0) {
                final RawCall secCall = secCallsStack.pop();
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

  private void setChangedFunctionsCount(final Diff diff) {
    int count = 0;
    for (final RawFunction function : diff.getCallgraph(ESide.PRIMARY).getNodes()) {
      if (function.isChanged()) {
        ++count;
      }
    }

    diff.getMatches().setSizeOfChangedFunctions(count);
  }

  private void setFunctionMatches(final Diff diff) {
    final RawCallGraph priCallgraph = diff.getCallgraph(ESide.PRIMARY);
    final RawCallGraph secCallgraph = diff.getCallgraph(ESide.SECONDARY);

    for (final FunctionMatchData functionMatch : diff.getMatches().getFunctionMatches()) {
      final RawFunction priFunction =
          priCallgraph.getFunction(functionMatch.getIAddress(ESide.PRIMARY));
      final RawFunction secFunction =
          secCallgraph.getFunction(functionMatch.getIAddress(ESide.SECONDARY));

      if (priFunction != null && secFunction != null) {
        priFunction.setMatch(secFunction, functionMatch);
        secFunction.setMatch(priFunction, functionMatch);
      }
    }
  }

  private void setSubDescription(final String description) {
    if (descriptionTarget != null) {
      descriptionTarget.setSubDescription(description);
    }
  }

  private void validateInputFiles(final Diff diff) throws IOException {
    if (!diff.getMatchesDatabase().exists()) {
      throw new IOException("Load Diff graphs failed. Matches dababase file can not be found.");
    }
    if (!diff.getExportFile(ESide.PRIMARY).exists()) {
      throw new IOException("Load Diff graphs failed. Primary exported file can not be found.");
    }
    if (!diff.getExportFile(ESide.SECONDARY).exists()) {
      throw new IOException("Load Diff graphs failed. Secondary exported file can not be found.");
    }
    if (!diff.getExportFile(ESide.PRIMARY).canRead()) {
      throw new IOException(
          "Load Diff graphs failed. Couldn't read primary exported call graph file.");
    }
    if (!diff.getExportFile(ESide.SECONDARY).canRead()) {
      throw new IOException(
          "Load Diff graphs failed. Couldn't read secondary exported call graph file.");
    }
  }

  @Override
  public void execute() throws Exception {
    for (final Diff diff : diffs) {
      loadDiff(diff);
    }
  }

  public void loadDiff(final Diff diff, final FunctionDiffSocketXmlData data) throws IOException {
    if (diff.isLoaded()) {
      Logger.logInfo("Single function diff is already loaded! Loading process canceled.");
      return;
    }

    if (descriptionTarget != null) {
      descriptionTarget.setDescription(String.format("Loading Diff '%s'", diff.getDiffName()));
    }
    Logger.logInfo("Loading single function diff '%s'", diff.getDiffName());
    validateInputFiles(diff);

    boolean loadfailed = false;
    try {
      loadSingleFunctionDiffRawCallgraphs(diff, data);

      setSubDescription("Loading function matches...");
      loadDiffMatches(diff);

      setSubDescription("Setting function matches...");
      setFunctionMatches(diff);

      setChangedFunctionsCount(diff);
    } catch (final IOException | SQLException e) {
      loadfailed = true;
      Logger.logException(e, "");
      throw new IOException("Load Diff graphs failed: " + e.getMessage());
    } finally {
      diff.setLoaded(!loadfailed);
    }

    setSubDescription("Preparing UI...");

    if (diff.isFunctionDiff()) {
      // GUI components are created and/or updated here. This must happen from the outside
      // the progress thread, or at least must be called by invokeLater!
      SwingUtilities.invokeLater(
          new Runnable() {
            @Override
            public void run() {
              for (final IDiffListener listener : diff.getListener()) {
                listener.loadedDiff(diff);
              }
            }
          });
    }
  }

  public void setProgressDescriptionTarget(final IProgressDescription descriptionTarget) {
    this.descriptionTarget = descriptionTarget;
  }
}
