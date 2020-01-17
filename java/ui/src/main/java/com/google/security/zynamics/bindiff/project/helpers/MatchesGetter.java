// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.project.helpers;

import com.google.security.zynamics.bindiff.enums.EMatchType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.sorters.AddressPairSorter;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.matches.InstructionMatchData;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawInstruction;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MatchesGetter {
  public static Map<IAddress, IAddress> createMatchedInstructionsMap(
      final Diff diff, final RawFunction function) {
    final Map<IAddress, IAddress> instructionMatches = new HashMap<>();

    final FunctionMatchData functionMatch = MatchesGetter.getFunctionMatch(diff, function);

    for (final BasicBlockMatchData basicblockMatch : functionMatch.getBasicBlockMatches()) {
      for (final InstructionMatchData instructionMatch : basicblockMatch.getInstructionMatches()) {
        if (function.getSide() == ESide.PRIMARY) {
          instructionMatches.put(
              instructionMatch.getIAddress(ESide.PRIMARY),
              instructionMatch.getIAddress(ESide.SECONDARY));
        } else {
          instructionMatches.put(
              instructionMatch.getIAddress(ESide.SECONDARY),
              instructionMatch.getIAddress(ESide.PRIMARY));
        }
      }
    }

    return instructionMatches;
  }

  public static List<Pair<IAddress, IAddress>> getBasicBlockAddressPairs(
      final Diff diff, final RawFlowGraph priFlowGraph, final RawFlowGraph secFlowGraph) {
    final List<Pair<IAddress, IAddress>> addrPairs = new ArrayList<>();

    final MatchData matches = diff.getMatches();

    if (priFlowGraph == null) {
      for (final RawBasicBlock basicBlock : secFlowGraph) {
        addrPairs.add(new Pair<>(null, basicBlock.getAddress()));
      }
    } else if (secFlowGraph == null) {
      for (final RawBasicBlock basicBlock : priFlowGraph) {
        addrPairs.add(new Pair<>(basicBlock.getAddress(), null));
      }
    } else {
      final FunctionMatchData functionMatch =
          matches.getFunctionMatch(priFlowGraph.getAddress(), ESide.PRIMARY);

      for (final RawBasicBlock priBasicBlock : priFlowGraph) {
        final IAddress priAddr = priBasicBlock.getAddress();
        final IAddress secAddr = functionMatch.getSecondaryBasicblockAddr(priAddr);

        addrPairs.add(new Pair<>(priAddr, secAddr));
      }

      for (final RawBasicBlock secBasicBlock : secFlowGraph) {
        final IAddress secAddr = secBasicBlock.getAddress();

        if (functionMatch.getPrimaryBasicblockAddr(secAddr) == null) {
          addrPairs.add(new Pair<>(null, secAddr));
        }
      }
    }

    return AddressPairSorter.getSortedList(addrPairs, ESide.PRIMARY);
  }

  public static BasicBlockMatchData getBasicBlockMatch(
      final Diff diff, final RawBasicBlock basicBlock) {
    final ESide side = basicBlock.getSide();
    final RawFunction rawFunction = diff.getFunction(basicBlock.getFunctionAddr(), side);
    final FunctionMatchData functionMatch = getFunctionMatch(diff, rawFunction);

    return getBasicBlockMatch(functionMatch, basicBlock);
  }

  public static BasicBlockMatchData getBasicBlockMatch(
      final Diff diff,
      final IAddress functionAddr,
      final IAddress basicBlockAddr,
      final ESide side) {
    final RawFunction rawFunction = diff.getFunction(functionAddr, side);
    final FunctionMatchData functionMatch = getFunctionMatch(diff, rawFunction);

    if (functionMatch == null) {
      return null;
    }

    return functionMatch.getBasicBlockMatch(basicBlockAddr, side);
  }

  public static BasicBlockMatchData getBasicBlockMatch(
      final FunctionMatchData functionMatch, final RawBasicBlock basicBlock) {
    if (functionMatch == null) {
      return null;
    }

    IAddress priAddr = basicBlock.getAddress();
    if (basicBlock.getSide() == ESide.SECONDARY) {
      priAddr = functionMatch.getPrimaryBasicblockAddr(priAddr);
    }

    return functionMatch.getBasicBlockMatch(priAddr, ESide.PRIMARY);
  }

  public static EMatchType getFlowGraphsMatchType(
      final CombinedGraph combinedFlowGraph, final FunctionMatchData functionMatch) {
    final int basicBlockMatchCount = functionMatch.getBasicBlockMatches().size();
    final int primaryBasicBlockCount =
        combinedFlowGraph.getPrimaryGraph().getPrimaryGraph().getNodes().size();
    final int secondaryBasicBlockCount =
        combinedFlowGraph.getSecondaryGraph().getSecondaryGraph().getNodes().size();

    if (primaryBasicBlockCount == basicBlockMatchCount
        && secondaryBasicBlockCount == basicBlockMatchCount) {
      boolean structuralIdenticalEdges = true;

      for (final CombinedDiffEdge combinedEdge : combinedFlowGraph.getEdges()) {
        final SingleDiffEdge priEdge = combinedEdge.getPrimaryDiffEdge();
        final SingleDiffEdge secEdge = combinedEdge.getSecondaryDiffEdge();

        if (priEdge == null || secEdge == null) {
          structuralIdenticalEdges = false;

          break;
        }
      }

      if (structuralIdenticalEdges) {
        boolean identicalInstructions = true;

        for (final CombinedDiffNode combinedNode : combinedFlowGraph.getNodes()) {
          final RawBasicBlock priRawNode = (RawBasicBlock) combinedNode.getPrimaryRawNode();
          final RawBasicBlock secRawNode = (RawBasicBlock) combinedNode.getSecondaryRawNode();

          final BasicBlockMatchData basicBlockMatch =
              functionMatch.getBasicBlockMatch(priRawNode.getAddress(), ESide.PRIMARY);

          final int priInstructionsCount = priRawNode.getSizeOfInstructions();
          final int secInstructionsCount = secRawNode.getSizeOfInstructions();
          final int matchedInstructionsCount = basicBlockMatch.getSizeOfMatchedInstructions();

          if (priInstructionsCount != matchedInstructionsCount
              || secInstructionsCount != matchedInstructionsCount) {

            identicalInstructions = false;

            break;
          }
        }

        if (identicalInstructions) {
          return EMatchType.IDENTICAL;
        }

        return EMatchType.INSTRUCTIONS_CHANGED;
      }
    }

    return EMatchType.STRUCTURAL_CHANGED;
  }

  public static FunctionMatchData getFunctionMatch(final Diff diff, final RawFlowGraph flowGraph) {
    final ESide side = flowGraph.getSide();
    final IAddress functionAddr = flowGraph.getAddress();
    final RawFunction function = diff.getCallGraph(side).getFunction(functionAddr);

    return getFunctionMatch(diff, function);
  }

  public static FunctionMatchData getFunctionMatch(final Diff diff, final RawFunction rawFunction) {
    final MatchData matches = diff.getMatches();

    if (rawFunction.getSide() == ESide.PRIMARY) {
      final IAddress priFunctionAddr = rawFunction.getAddress();
      final IAddress secFunctionAddr = matches.getSecondaryFunctionAddr(rawFunction.getAddress());

      if (secFunctionAddr != null) {
        return matches.getFunctionMatch(priFunctionAddr, ESide.PRIMARY);
      }
    } else {
      final IAddress priFunctionAddr = matches.getPrimaryFunctionAddr(rawFunction.getAddress());

      if (priFunctionAddr != null) {
        return matches.getFunctionMatch(priFunctionAddr, ESide.PRIMARY);
      }
    }

    return null;
  }

  public static List<Pair<IAddress, IAddress>> getInstructionAddressPairs(
      final Diff diff, final RawBasicBlock priBasicBlock, final RawBasicBlock secBasicBlock) {
    final List<Pair<IAddress, IAddress>> addrPairs = new ArrayList<>();

    final MatchData matches = diff.getMatches();

    if (priBasicBlock == null) {
      for (final RawInstruction instruction : secBasicBlock) {
        addrPairs.add(new Pair<>(null, instruction.getAddress()));
      }
    } else if (secBasicBlock == null) {
      for (final RawInstruction instruction : priBasicBlock) {
        addrPairs.add(new Pair<>(instruction.getAddress(), null));
      }
    } else {
      final IAddress priFunctionAddr = priBasicBlock.getFunctionAddr();
      final FunctionMatchData functionMatch =
          matches.getFunctionMatch(priFunctionAddr, ESide.PRIMARY);

      final IAddress priBasicblockAddr = priBasicBlock.getAddress();
      final BasicBlockMatchData basicblockMatch =
          functionMatch.getBasicBlockMatch(priBasicblockAddr, ESide.PRIMARY);

      for (final RawInstruction priInstruction : priBasicBlock) {
        final IAddress priAddr = priInstruction.getAddress();
        final IAddress secAddr = basicblockMatch.getSecondaryInstructionAddr(priAddr);

        addrPairs.add(new Pair<>(priAddr, secAddr));
      }

      for (final RawInstruction secInstruction : secBasicBlock) {
        final IAddress secAddr = secInstruction.getAddress();

        if (basicblockMatch.getPrimaryInstructionAddr(secAddr) == null) {
          addrPairs.add(new Pair<>(null, secAddr));
        }
      }
    }

    return AddressPairSorter.getSortedList(addrPairs, ESide.PRIMARY);
  }

  public static boolean isChangedBasicBlock(
      final Diff diff, final RawBasicBlock priBasicblock, final RawBasicBlock secBasicblock) {
    if (priBasicblock == null || secBasicblock == null) {
      return false;
    }

    final BasicBlockMatchData basicblockMatch =
        MatchesGetter.getBasicBlockMatch(diff, priBasicblock);

    if (basicblockMatch != null) {
      final int matchedInstrCount = basicblockMatch.getSizeOfMatchedInstructions();
      final int primaryInstrCount = priBasicblock.getSizeOfInstructions();
      final int secondaryInstrCount = secBasicblock.getSizeOfInstructions();

      return matchedInstrCount != primaryInstrCount || matchedInstrCount != secondaryInstrCount;
    }

    return false;
  }

  public static boolean isIdenticalBasicBlock(
      final Diff diff, final RawBasicBlock priBasicblock, final RawBasicBlock secBasicblock) {
    if (priBasicblock == null || secBasicblock == null) {
      return false;
    }

    final BasicBlockMatchData basicBlockMatch =
        MatchesGetter.getBasicBlockMatch(diff, priBasicblock);

    if (basicBlockMatch != null) {
      final int matchedInstrCount = basicBlockMatch.getSizeOfMatchedInstructions();
      final int primaryInstrCount = priBasicblock.getSizeOfInstructions();
      final int secondaryInstrCount = secBasicblock.getSizeOfInstructions();

      return matchedInstrCount == primaryInstrCount && matchedInstrCount == secondaryInstrCount;
    }

    return false;
  }

  public static boolean isIdenticalFunctionPair(
      final RawFunction primaryFunction, final RawFunction secondaryFunction) {
    if (primaryFunction.getMatchedFunction() != secondaryFunction) {
      throw new IllegalArgumentException(
          "Primary and secondary function must be matched to each other.");
    }

    return primaryFunction.isIdenticalMatch();
  }

  public static boolean isInstructionsOnlyChangedFunctionPair(
      final RawFunction primaryFunction, final RawFunction secondaryFunction) {
    if (primaryFunction.getMatchedFunction() != secondaryFunction) {
      throw new IllegalArgumentException(
          "Primary and secondary function must be matched to each other.");
    }

    final int matchedInstructions =
        primaryFunction.getFunctionMatch().getSizeOfMatchedInstructions();

    return primaryFunction.getSizeOfUnmatchedBasicblocks() == 0
        && primaryFunction.getSizeOfUnmatchedJumps() == 0
        && secondaryFunction.getSizeOfUnmatchedBasicblocks() == 0
        && secondaryFunction.getSizeOfUnmatchedJumps() == 0
        && (primaryFunction.getSizeOfInstructions() != matchedInstructions
            || secondaryFunction.getSizeOfInstructions() != matchedInstructions);
  }

  public static boolean isMatchedInstruction(
      final Diff diff, final RawBasicBlock basicBlock, final RawInstruction instruction) {
    final MatchData matches = diff.getMatches();

    final FunctionMatchData functionMatch =
        matches.getFunctionMatch(basicBlock.getFunctionAddr(), basicBlock.getSide());

    return isMatchedInstruction(functionMatch, basicBlock, instruction);
  }

  public static boolean isMatchedInstruction(
      final FunctionMatchData functionMatch,
      final RawBasicBlock basicblock,
      final RawInstruction instruction) {
    final ESide side = basicblock.getSide();

    if (functionMatch != null) {
      final IAddress priBasicBlockAddr =
          side == ESide.PRIMARY
              ? basicblock.getAddress()
              : functionMatch.getPrimaryBasicblockAddr(basicblock.getAddress());
      final IAddress secBasicBlockAddr =
          side == ESide.SECONDARY
              ? basicblock.getAddress()
              : functionMatch.getSecondaryBasicblockAddr(basicblock.getAddress());

      if (priBasicBlockAddr != null && secBasicBlockAddr != null) {
        final BasicBlockMatchData basicBlockMatch =
            functionMatch.getBasicBlockMatch(priBasicBlockAddr, ESide.PRIMARY);

        if (basicBlockMatch != null) {
          final IAddress instructionAddr = instruction.getAddress();

          if (side == ESide.PRIMARY) {
            return basicBlockMatch.getSecondaryInstructionAddr(instructionAddr) != null;
          }

          return basicBlockMatch.getPrimaryInstructionAddr(instructionAddr) != null;
        }
      }
    }

    return false;
  }

  public static boolean isStructuralChangedFunctionPair(
      final RawFunction primaryFunction, final RawFunction secondaryFunction) {
    if (primaryFunction.getMatchedFunction() != secondaryFunction) {
      throw new IllegalArgumentException(
          "Primary and secondary function must be matched to each other.");
    }

    return primaryFunction.getSizeOfUnmatchedBasicblocks() != 0
        || primaryFunction.getSizeOfUnmatchedJumps() != 0
        || secondaryFunction.getSizeOfUnmatchedBasicblocks() != 0
        || secondaryFunction.getSizeOfUnmatchedJumps() != 0;
  }
}
