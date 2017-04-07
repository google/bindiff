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

    for (final BasicBlockMatchData basicblockMatch : functionMatch.getBasicblockMatches()) {
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

  public static List<Pair<IAddress, IAddress>> getBasicblockAddressPairs(
      final Diff diff, final RawFlowGraph priFlowgraph, final RawFlowGraph secFlowgraph) {
    final List<Pair<IAddress, IAddress>> addrPairs = new ArrayList<>();

    final MatchData matches = diff.getMatches();

    if (priFlowgraph == null) {
      for (final RawBasicBlock basicblock : secFlowgraph) {
        addrPairs.add(new Pair<>(null, basicblock.getAddress()));
      }
    } else if (secFlowgraph == null) {
      for (final RawBasicBlock basicblock : priFlowgraph) {
        addrPairs.add(new Pair<>(basicblock.getAddress(), null));
      }
    } else {
      final FunctionMatchData functionMatch =
          matches.getFunctionMatch(priFlowgraph.getAddress(), ESide.PRIMARY);

      for (final RawBasicBlock priBasicblock : priFlowgraph) {
        final IAddress priAddr = priBasicblock.getAddress();
        final IAddress secAddr = functionMatch.getSecondaryBasicblockAddr(priAddr);

        addrPairs.add(new Pair<>(priAddr, secAddr));
      }

      for (final RawBasicBlock secBasicblock : secFlowgraph) {
        final IAddress secAddr = secBasicblock.getAddress();

        if (functionMatch.getPrimaryBasicblockAddr(secAddr) == null) {
          addrPairs.add(new Pair<>(null, secAddr));
        }
      }
    }

    return AddressPairSorter.getSortedList(addrPairs, ESide.PRIMARY);
  }

  public static BasicBlockMatchData getBasicblockMatch(
      final Diff diff, final RawBasicBlock basicblock) {
    final ESide side = basicblock.getSide();

    RawFunction rawFunction = null;
    if (side == ESide.PRIMARY) {
      rawFunction = diff.getFunction(basicblock.getFunctionAddr(), ESide.PRIMARY);
    } else {
      rawFunction = diff.getFunction(basicblock.getFunctionAddr(), ESide.SECONDARY);
    }

    final FunctionMatchData functionMatch = getFunctionMatch(diff, rawFunction);

    return getBasicblockMatch(functionMatch, basicblock);
  }

  public static BasicBlockMatchData getBasicblockMatch(
      final Diff diff,
      final IAddress functionAddr,
      final IAddress basicblockAddr,
      final ESide side) {
    RawFunction rawFunction = null;
    rawFunction = diff.getFunction(functionAddr, side);

    final FunctionMatchData functionMatch = getFunctionMatch(diff, rawFunction);

    if (functionMatch == null) {
      return null;
    }

    IAddress priAddr = null;
    IAddress secAddr = null;

    if (side == ESide.PRIMARY) {
      priAddr = basicblockAddr;
      secAddr = functionMatch.getSecondaryBasicblockAddr(priAddr);
    } else {
      secAddr = basicblockAddr;
      priAddr = functionMatch.getPrimaryBasicblockAddr(secAddr);
    }

    return functionMatch.getBasicblockMatch(priAddr, ESide.PRIMARY);
  }

  public static BasicBlockMatchData getBasicblockMatch(
      final FunctionMatchData functionMatch, final RawBasicBlock basicblock) {
    if (functionMatch == null) {
      return null;
    }

    IAddress priAddr = null;
    IAddress secAddr = null;
    if (basicblock.getSide() == ESide.PRIMARY) {
      priAddr = basicblock.getAddress();
      secAddr = functionMatch.getSecondaryBasicblockAddr(priAddr);
    } else {
      secAddr = basicblock.getAddress();
      priAddr = functionMatch.getPrimaryBasicblockAddr(secAddr);
    }

    return functionMatch.getBasicblockMatch(priAddr, ESide.PRIMARY);
  }

  public static EMatchType getFlowgraphsMatchType(
      final CombinedGraph combinedFlowgraph, final FunctionMatchData functionMatch) {
    final int basicblockMatchCount = functionMatch.getBasicblockMatches().size();
    final int primaryBasicblockCount =
        combinedFlowgraph.getPrimaryGraph().getPrimaryGraph().getNodes().size();
    final int secondaryBasicblockCount =
        combinedFlowgraph.getSecondaryGraph().getSecondaryGraph().getNodes().size();

    if (primaryBasicblockCount == basicblockMatchCount
        && secondaryBasicblockCount == basicblockMatchCount) {
      boolean structuralIdenticalEdges = true;

      for (final CombinedDiffEdge combinedEdge : combinedFlowgraph.getEdges()) {
        final SingleDiffEdge priEdge = combinedEdge.getPrimaryDiffEdge();
        final SingleDiffEdge secEdge = combinedEdge.getSecondaryDiffEdge();

        if (priEdge == null || secEdge == null) {
          structuralIdenticalEdges = false;

          break;
        }
      }

      if (structuralIdenticalEdges) {
        boolean identicalInstructions = true;

        for (final CombinedDiffNode combinedNode : combinedFlowgraph.getNodes()) {
          final RawBasicBlock priRawNode = (RawBasicBlock) combinedNode.getPrimaryRawNode();
          final RawBasicBlock secRawNode = (RawBasicBlock) combinedNode.getSecondaryRawNode();

          final BasicBlockMatchData basicblockMatch =
              functionMatch.getBasicblockMatch(priRawNode.getAddress(), ESide.PRIMARY);

          final int priInstructionsCount = priRawNode.getSizeOfInstructions();
          final int secInstructionsCount = secRawNode.getSizeOfInstructions();
          final int matchedInstructionsCount = basicblockMatch.getSizeOfMatchedInstructions();

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

  public static FunctionMatchData getFunctionMatch(final Diff diff, final RawFlowGraph flowgraph) {
    final ESide side = flowgraph.getSide();
    final IAddress functionAddr = flowgraph.getAddress();
    final RawFunction function = diff.getCallgraph(side).getFunction(functionAddr);

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
      final Diff diff, final RawBasicBlock priBasicblock, final RawBasicBlock secBasicblock) {
    final List<Pair<IAddress, IAddress>> addrPairs = new ArrayList<>();

    final MatchData matches = diff.getMatches();

    if (priBasicblock == null) {
      for (final RawInstruction instruction : secBasicblock) {
        addrPairs.add(new Pair<>(null, instruction.getAddress()));
      }
    } else if (secBasicblock == null) {
      for (final RawInstruction instruction : priBasicblock) {
        addrPairs.add(new Pair<>(instruction.getAddress(), null));
      }
    } else {
      final IAddress priFunctionAddr = priBasicblock.getFunctionAddr();
      final FunctionMatchData functionMatch =
          matches.getFunctionMatch(priFunctionAddr, ESide.PRIMARY);

      final IAddress priBasicblockAddr = priBasicblock.getAddress();
      final BasicBlockMatchData basicblockMatch =
          functionMatch.getBasicblockMatch(priBasicblockAddr, ESide.PRIMARY);

      for (final RawInstruction priInstruction : priBasicblock) {
        final IAddress priAddr = priInstruction.getAddress();
        final IAddress secAddr = basicblockMatch.getSecondaryInstructionAddr(priAddr);

        addrPairs.add(new Pair<>(priAddr, secAddr));
      }

      for (final RawInstruction secInstruction : secBasicblock) {
        final IAddress secAddr = secInstruction.getAddress();

        if (basicblockMatch.getPrimaryInstructionAddr(secAddr) == null) {
          addrPairs.add(new Pair<>(null, secAddr));
        }
      }
    }

    return AddressPairSorter.getSortedList(addrPairs, ESide.PRIMARY);
  }

  public static boolean isChangedBasicblock(
      final Diff diff, final RawBasicBlock priBasicblock, final RawBasicBlock secBasicblock) {
    if (priBasicblock == null || secBasicblock == null) {
      return false;
    }

    final BasicBlockMatchData basicblockMatch =
        MatchesGetter.getBasicblockMatch(diff, priBasicblock);

    if (basicblockMatch != null) {
      final int matchedInstrCount = basicblockMatch.getSizeOfMatchedInstructions();
      final int primaryInstrCount = priBasicblock.getSizeOfInstructions();
      final int secondaryInstrCount = secBasicblock.getSizeOfInstructions();

      return matchedInstrCount != primaryInstrCount || matchedInstrCount != secondaryInstrCount;
    }

    return false;
  }

  public static boolean isIdenticalBasicblock(
      final Diff diff, final RawBasicBlock priBasicblock, final RawBasicBlock secBasicblock) {
    if (priBasicblock == null || secBasicblock == null) {
      return false;
    }

    final BasicBlockMatchData basicblockMatch =
        MatchesGetter.getBasicblockMatch(diff, priBasicblock);

    if (basicblockMatch != null) {
      final int matchedInstrCount = basicblockMatch.getSizeOfMatchedInstructions();
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
      final Diff diff, final RawBasicBlock basicblock, final RawInstruction instruction) {
    final MatchData matches = diff.getMatches();

    final FunctionMatchData functionMatch =
        matches.getFunctionMatch(basicblock.getFunctionAddr(), basicblock.getSide());

    return isMatchedInstruction(functionMatch, basicblock, instruction);
  }

  public static boolean isMatchedInstruction(
      final FunctionMatchData functionMatch,
      final RawBasicBlock basicblock,
      final RawInstruction instruction) {
    final ESide side = basicblock.getSide();

    if (functionMatch != null) {
      final IAddress priBasicblockAddr =
          side == ESide.PRIMARY
              ? basicblock.getAddress()
              : functionMatch.getPrimaryBasicblockAddr(basicblock.getAddress());
      final IAddress secBasicblockAddr =
          side == ESide.SECONDARY
              ? basicblock.getAddress()
              : functionMatch.getSecondaryBasicblockAddr(basicblock.getAddress());

      if (priBasicblockAddr != null && secBasicblockAddr != null) {
        final BasicBlockMatchData basicblockMatch =
            functionMatch.getBasicblockMatch(priBasicblockAddr, ESide.PRIMARY);

        if (basicblockMatch != null) {
          final IAddress instructionAddr = instruction.getAddress();

          if (side == ESide.PRIMARY) {
            return basicblockMatch.getSecondaryInstructionAddr(instructionAddr) != null;
          }

          return basicblockMatch.getPrimaryInstructionAddr(instructionAddr) != null;
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
