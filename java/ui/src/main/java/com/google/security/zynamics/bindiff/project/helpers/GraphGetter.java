// Copyright 2011-2021 Google LLC
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

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawJump;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GraphGetter {
  public static RawCall getCall(
      final RawCallGraph callgraph,
      final IAddress sourceFunctionAddr,
      final IAddress targetFunctionAddr,
      final IAddress sourceInstructionAddr) {
    final RawFunction function = callgraph.getFunction(sourceFunctionAddr);

    if (function != null) {
      for (final SingleViewEdge<? extends SingleViewNode> outgoingCall :
          function.getOutgoingEdges()) {
        @SuppressWarnings("unchecked")
        final RawCall call = (RawCall) outgoingCall;

        if (call.getSourceInstructionAddr().equals(sourceInstructionAddr)) {
          if (call.getTarget().getAddress().equals(targetFunctionAddr)) {
            return call;
          }
        }
      }
    }

    return null;
  }

  public static List<RawFunction> getCallees(final RawFunction function) {
    final List<RawFunction> callees = new ArrayList<>();
    callees.addAll(function.getCallees());

    return callees;
  }

  public static List<RawFunction> getCallers(final RawFunction function) {
    final List<RawFunction> callers = new ArrayList<>();
    callers.addAll(function.getCallers());

    return callers;
  }

  public static List<RawCall> getCallsAt(
      final RawCallGraph callgraph,
      final IAddress sourceFunctionAddr,
      final IAddress sourceInstructionAddr) {
    final List<RawCall> calls = new ArrayList<>();

    final RawFunction function = callgraph.getFunction(sourceFunctionAddr);

    for (final SingleViewEdge<? extends SingleViewNode> outgoingCall :
        function.getOutgoingEdges()) {
      @SuppressWarnings("unchecked")
      final RawCall call = (RawCall) outgoingCall;

      if (call.getSourceInstructionAddr().equals(sourceInstructionAddr)) {
        calls.add(call);
      }
    }

    return calls;
  }

  public static Set<Pair<RawFunction, RawFunction>> getChangedFunctionPairs(
      final RawCallGraph priCallgraph, final RawCallGraph secCallgraph) {
    final Set<Pair<RawFunction, RawFunction>> allFunctions;
    allFunctions = GraphGetter.getMatchedFunctionPairs(priCallgraph, secCallgraph);

    final Set<Pair<RawFunction, RawFunction>> changedFunctions = new HashSet<>();

    for (final Pair<RawFunction, RawFunction> functionMatch : allFunctions) {
      final RawFunction primaryFunction = functionMatch.first();
      final RawFunction secondaryFunction = functionMatch.second();

      final int matchedInstructions =
          primaryFunction.getFunctionMatch().getSizeOfMatchedInstructions();

      if (!(primaryFunction.getSizeOfUnmatchedBasicBlocks() == 0
          && primaryFunction.getSizeOfUnmatchedJumps() == 0
          && secondaryFunction.getSizeOfUnmatchedBasicBlocks() == 0
          && secondaryFunction.getSizeOfUnmatchedJumps() == 0
          && primaryFunction.getSizeOfInstructions() == matchedInstructions
          && secondaryFunction.getSizeOfInstructions() == matchedInstructions)) {
        final Pair<RawFunction, RawFunction> functionPair =
            new Pair<>(primaryFunction, secondaryFunction);

        changedFunctions.add(functionPair);
      }
    }

    return changedFunctions;
  }

  public static SingleDiffNode getDiffNode(
      final GraphsContainer graphs, final RawBasicBlock basicblock) {
    return basicblock.getSide() == ESide.PRIMARY
        ? graphs.getPrimaryGraph().getNode(basicblock)
        : graphs.getSecondaryGraph().getNode(basicblock);
  }

  public static RawFunction getFunction(final Diff diff, final RawBasicBlock basicblock) {
    final IAddress functionAddr = basicblock.getFunctionAddr();
    final RawCallGraph callgraph = diff.getCallGraph(basicblock.getSide());

    return callgraph.getFunction(functionAddr);
  }

  public static RawFunction getFunction(final Diff diff, final RawFlowGraph flowgraph) {
    if (flowgraph == null) {
      return null;
    }

    final RawCallGraph callgraph = diff.getCallGraph(flowgraph.getSide());

    return callgraph.getFunction(flowgraph.getAddress());
  }

  public static Set<Pair<RawFunction, RawFunction>> getIdenticalFunctionPairs(
      final RawCallGraph priCallgraph, final RawCallGraph secCallgraph) {
    final Set<Pair<RawFunction, RawFunction>> allFunctions;
    allFunctions = GraphGetter.getMatchedFunctionPairs(priCallgraph, secCallgraph);

    final Set<Pair<RawFunction, RawFunction>> identicalFunctions = new HashSet<>();
    for (final Pair<RawFunction, RawFunction> functionMatch : allFunctions) {
      final RawFunction primaryFunction = functionMatch.first();
      final RawFunction secondaryFunction = functionMatch.second();

      if (MatchesGetter.isIdenticalFunctionPair(primaryFunction, secondaryFunction)) {
        final Pair<RawFunction, RawFunction> functionPair =
            new Pair<>(primaryFunction, secondaryFunction);
        identicalFunctions.add(functionPair);
      }
    }

    return identicalFunctions;
  }

  public static Collection<RawJump> getIncomingJumps(final RawBasicBlock basicblock) {
    final Collection<RawJump> rawJumps = new ArrayList<>();

    for (final SingleViewEdge<? extends SingleViewNode> jump : basicblock.getIncomingEdges()) {
      if (jump instanceof RawJump) {
        rawJumps.add((RawJump) jump);
      }
    }

    return rawJumps;
  }

  public static Set<Pair<RawFunction, RawFunction>> getInstructionOnlyChangedFunctionPairs(
      final RawCallGraph priCallgraph, final RawCallGraph secCallgraph) {
    final Set<Pair<RawFunction, RawFunction>> allFunctions;
    allFunctions = GraphGetter.getMatchedFunctionPairs(priCallgraph, secCallgraph);

    final Set<Pair<RawFunction, RawFunction>> changedFunctions = new HashSet<>();

    for (final Pair<RawFunction, RawFunction> functionMatch : allFunctions) {
      final RawFunction primaryFunction = functionMatch.first();
      final RawFunction secondaryFunction = functionMatch.second();

      if (MatchesGetter.isInstructionsOnlyChangedFunctionPair(primaryFunction, secondaryFunction)) {
        final Pair<RawFunction, RawFunction> functionPair =
            new Pair<>(primaryFunction, secondaryFunction);

        changedFunctions.add(functionPair);
      }
    }

    return changedFunctions;
  }

  public static List<RawFunction> getMatchedCallees(final RawFunction function) {
    final Set<RawFunction> machtedCallees = new HashSet<>();

    for (final RawFunction callee : function.getCallees()) {
      if (callee.getMatchState() == EMatchState.MATCHED) {
        machtedCallees.add(callee);
      }
    }

    final List<RawFunction> list = new ArrayList<>();
    list.addAll(machtedCallees);

    return list;
  }

  public static List<RawFunction> getMatchedCallers(final RawFunction function) {
    final Set<RawFunction> matchedCallers = new HashSet<>();

    for (final RawFunction caller : function.getCallers()) {
      if (caller.getMatchState() == EMatchState.MATCHED) {
        matchedCallers.add(caller);
      }
    }

    final List<RawFunction> list = new ArrayList<>();
    list.addAll(matchedCallers);

    return list;
  }

  public static Set<Pair<RawFunction, RawFunction>> getMatchedFunctionPairs(
      final RawCallGraph priCallgraph, final RawCallGraph secCallgraph) {
    final Set<Pair<RawFunction, RawFunction>> matchedFunctions = new HashSet<>();

    for (final RawFunction primaryFunction : priCallgraph.getNodes()) {
      if (primaryFunction.getMatchState() == EMatchState.MATCHED) {
        final RawFunction secondaryFunction =
            secCallgraph.getFunction(primaryFunction.getMatchedFunctionAddress());

        Pair<RawFunction, RawFunction> functionPair;
        functionPair = new Pair<RawFunction, RawFunction>(primaryFunction, secondaryFunction);

        matchedFunctions.add(functionPair);
      }
    }

    return matchedFunctions;
  }

  public static List<RawCall> getMatchedIncomingCalls(final RawFunction function) {
    final List<RawCall> matchedCalls = new ArrayList<>();
    for (final SingleViewEdge<? extends SingleViewNode> incomingCall :
        function.getIncomingEdges()) {
      @SuppressWarnings("unchecked")
      final RawCall call = (RawCall) incomingCall;

      if (call.getMatchState() == EMatchState.MATCHED || call.isChanged()) {
        matchedCalls.add(call);
      }
    }

    return matchedCalls;
  }

  public static Collection<RawJump> getMatchedIncomingJumps(
      final Diff diff,
      final RawFlowGraph priFlowgraph,
      final RawFlowGraph secFlowgraph,
      final RawBasicBlock basicblock) {
    final Collection<RawJump> matchedJumps = new ArrayList<>();

    final BasicBlockMatchData srcBasicblockMatch =
        MatchesGetter.getBasicBlockMatch(diff, basicblock);

    if (srcBasicblockMatch == null) {
      return matchedJumps;
    }

    RawFlowGraph otherFlowgraph;
    otherFlowgraph = basicblock.getSide() == ESide.PRIMARY ? secFlowgraph : priFlowgraph;

    final Collection<RawJump> jumps = getIncomingJumps(basicblock);

    for (final RawJump jump : jumps) {
      final RawBasicBlock tarBasicblock = jump.getTarget();

      final BasicBlockMatchData tarBasicblockMatch =
          MatchesGetter.getBasicBlockMatch(diff, tarBasicblock);

      if (tarBasicblockMatch != null) {
        IAddress otherSrcAddr = null;
        IAddress otherTarAddr = null;

        if (basicblock.getSide() == ESide.PRIMARY) {
          otherSrcAddr = srcBasicblockMatch.getIAddress(ESide.SECONDARY);
          otherTarAddr = tarBasicblockMatch.getIAddress(ESide.SECONDARY);
        } else {
          otherSrcAddr = srcBasicblockMatch.getIAddress(ESide.PRIMARY);
          otherTarAddr = tarBasicblockMatch.getIAddress(ESide.PRIMARY);
        }

        if (otherFlowgraph.getJump(otherSrcAddr, otherTarAddr) != null) {
          matchedJumps.add(jump);
        }
      }
    }

    return matchedJumps;
  }

  public static List<RawCall> getMatchedOutgoingCalls(
      final Diff diff, final RawBasicBlock basicblock) {
    final List<RawCall> matchedOutgoingCalls = new ArrayList<>();

    final List<RawCall> outgoingCalls = getOutgoingCalls(diff, basicblock);

    for (final RawCall call : outgoingCalls) {
      if (call.getMatchState() == EMatchState.MATCHED || call.isChanged()) {
        matchedOutgoingCalls.add(call);
      }
    }

    return matchedOutgoingCalls;
  }

  public static List<RawCall> getMatchedOutgoingCalls(final RawFunction function) {
    final List<RawCall> matchedCalls = new ArrayList<>();

    for (final SingleViewEdge<? extends SingleViewNode> outgoingCall :
        function.getOutgoingEdges()) {
      @SuppressWarnings("unchecked")
      final RawCall call = (RawCall) outgoingCall;
      if (call.getMatchState() == EMatchState.MATCHED || call.isChanged()) {
        matchedCalls.add(call);
      }
    }

    return matchedCalls;
  }

  public static Collection<RawJump> getMatchedOutgoingJumps(
      final Diff diff,
      final RawFlowGraph priFlowgraph,
      final RawFlowGraph secFlowgraph,
      final RawBasicBlock basicblock) {
    final Collection<RawJump> matchedJumps = new ArrayList<>();

    final BasicBlockMatchData srcBasicblockMatch =
        MatchesGetter.getBasicBlockMatch(diff, basicblock);

    if (srcBasicblockMatch == null) {
      return matchedJumps;
    }

    RawFlowGraph otherFlowgraph;
    otherFlowgraph = basicblock.getSide() == ESide.PRIMARY ? secFlowgraph : priFlowgraph;

    final Collection<RawJump> jumps = getOutgoingJumps(basicblock);

    for (final RawJump jump : jumps) {
      final RawBasicBlock tarBasicblock = jump.getTarget();

      final BasicBlockMatchData tarBasicblockMatch =
          MatchesGetter.getBasicBlockMatch(diff, tarBasicblock);

      if (tarBasicblockMatch != null) {
        IAddress otherSrcAddr = null;
        IAddress otherTarAddr = null;

        if (basicblock.getSide() == ESide.PRIMARY) {
          otherSrcAddr = srcBasicblockMatch.getIAddress(ESide.SECONDARY);
          otherTarAddr = tarBasicblockMatch.getIAddress(ESide.SECONDARY);
        } else {
          otherSrcAddr = srcBasicblockMatch.getIAddress(ESide.PRIMARY);
          otherTarAddr = tarBasicblockMatch.getIAddress(ESide.PRIMARY);
        }

        if (otherFlowgraph.getJump(otherSrcAddr, otherTarAddr) != null) {
          matchedJumps.add(jump);
        }
      }
    }

    return matchedJumps;
  }

  // TODO: This does not work properly with 64bit addresses
  public static IAddress getMaxAddress(final List<RawBasicBlock> basicblocks) {
    if (basicblocks == null || basicblocks.size() == 0) {
      return null;
    }

    IAddress maxAddr = new CAddress(0);

    for (final RawBasicBlock basicblock : basicblocks) {
      final IAddress addr = basicblock.getAddress();

      if (addr.compareTo(maxAddr) > 0) {
        maxAddr = addr;
      }
    }

    return maxAddr;
  }

  // TODO: This does not work properly with 64bit addresses
  public static IAddress getMinAddress(final List<RawBasicBlock> basicblocks) {
    if (basicblocks == null || basicblocks.size() == 0) {
      return null;
    }

    IAddress minAddr = new CAddress(0xFFFFFFFF);

    for (final RawBasicBlock basicblock : basicblocks) {
      final IAddress addr = basicblock.getAddress();

      if (addr.compareTo(minAddr) < 0) {
        minAddr = addr;
      }
    }

    return minAddr;
  }

  public static SingleGraph getOtherGraph(final SingleGraph graph) {
    return graph.getSide() == ESide.PRIMARY ? graph.getSecondaryGraph() : graph.getPrimaryGraph();
  }

  public static List<RawCall> getOutgoingCalls(final Diff diff, final RawBasicBlock basicblock) {
    final List<RawCall> basicblockCalls = new ArrayList<>();

    final RawFunction function = getFunction(diff, basicblock);

    for (final SingleViewEdge<? extends SingleViewNode> outgoingCall :
        function.getOutgoingEdges()) {
      @SuppressWarnings("unchecked")
      final RawCall call = (RawCall) outgoingCall;

      if (basicblock.getInstruction(call.getSourceInstructionAddr()) != null) {
        basicblockCalls.add(call);
      }
    }

    return basicblockCalls;
  }

  public static Collection<RawJump> getOutgoingJumps(final RawBasicBlock basicblock) {
    final Collection<RawJump> rawJumps = new ArrayList<>();

    for (final SingleViewEdge<? extends SingleViewNode> jump : basicblock.getOutgoingEdges()) {
      if (jump instanceof RawJump) {
        rawJumps.add((RawJump) jump);
      }
    }

    return rawJumps;
  }

  public static SingleDiffNode getPrimaryDiffNode(
      final SingleGraph graph, final RawBasicBlock secondaryBasicblock) {
    final SingleDiffNode diffNode = graph.getSecondaryGraph().getNode(secondaryBasicblock);

    return diffNode.getOtherSideDiffNode();
  }

  public static RawBasicBlock getPrimaryRawBasicblock(
      final GraphsContainer graphs, final RawBasicBlock secondaryBasicblock) {
    final SingleDiffNode diffNode = getDiffNode(graphs, secondaryBasicblock);
    final SingleDiffNode otherDiffNode = diffNode.getOtherSideDiffNode();

    if (otherDiffNode != null) {
      return (RawBasicBlock) otherDiffNode.getRawNode();
    }

    return null;
  }

  public static RawFunction getRawFunction(final Diff diff, final RawFlowGraph flowgraph) {
    return diff.getCallGraph(flowgraph.getSide()).getFunction(flowgraph.getAddress());
  }

  public static SingleDiffNode getSecondaryDiffNode(
      final SingleGraph graph, final RawBasicBlock primaryBasicblock) {
    final SingleDiffNode diffNode = graph.getPrimaryGraph().getNode(primaryBasicblock);

    return diffNode.getOtherSideDiffNode();
  }

  public static RawBasicBlock getSecondaryRawBasicblock(
      final GraphsContainer graphs, final RawBasicBlock primaryBasicblock) {
    final SingleDiffNode diffNode = getDiffNode(graphs, primaryBasicblock);
    final SingleDiffNode otherDiffNode = diffNode.getOtherSideDiffNode();

    if (otherDiffNode != null) {
      return (RawBasicBlock) otherDiffNode.getRawNode();
    }

    return null;
  }

  public static Set<Pair<RawFunction, RawFunction>> getStructuralChangedFunctionPairs(
      final RawCallGraph priCallgraph, final RawCallGraph secCallgraph) {
    final Set<Pair<RawFunction, RawFunction>> allFunctions;
    allFunctions = GraphGetter.getMatchedFunctionPairs(priCallgraph, secCallgraph);

    final Set<Pair<RawFunction, RawFunction>> changedFunctions = new HashSet<>();

    for (final Pair<RawFunction, RawFunction> functionMatch : allFunctions) {
      final RawFunction primaryFunction = functionMatch.first();
      final RawFunction secondaryFunction = functionMatch.second();

      if (MatchesGetter.isStructuralChangedFunctionPair(primaryFunction, secondaryFunction)) {
        final Pair<RawFunction, RawFunction> functionPair =
            new Pair<RawFunction, RawFunction>(primaryFunction, secondaryFunction);
        changedFunctions.add(functionPair);
      }
    }

    return changedFunctions;
  }

  public static List<RawFunction> getUnmatchedCallees(final RawFunction function) {
    final Set<RawFunction> unmatchedCallees = new HashSet<>();

    for (final RawFunction callee : function.getCallees()) {
      if (callee.getMatchState() != EMatchState.MATCHED) {
        unmatchedCallees.add(callee);
      }
    }

    final List<RawFunction> list = new ArrayList<>();
    list.addAll(unmatchedCallees);

    return list;
  }

  public static List<RawFunction> getUnmatchedCallers(final RawFunction function) {
    final Set<RawFunction> unmatchedCallers = new HashSet<>();

    for (final RawFunction caller : function.getCallees()) {
      if (caller.getMatchState() != EMatchState.MATCHED) {
        unmatchedCallers.add(caller);
      }
    }

    final List<RawFunction> list = new ArrayList<>();
    list.addAll(unmatchedCallers);

    return list;
  }

  public static List<RawFunction> getUnmatchedFunctions(final RawCallGraph callgraph) {
    final List<RawFunction> unmatchedFunctions = new ArrayList<>();

    for (final RawFunction function : callgraph.getNodes()) {
      if (function.getMatchState() != EMatchState.MATCHED) {
        unmatchedFunctions.add(function);
      }
    }

    return unmatchedFunctions;
  }

  public static List<RawCall> getUnmatchedIncomingCalls(final RawFunction function) {
    final List<RawCall> unmatchedCalls = new ArrayList<>();

    for (final SingleViewEdge<? extends SingleViewNode> incomingCall :
        function.getIncomingEdges()) {
      @SuppressWarnings("unchecked")
      final RawCall call = (RawCall) incomingCall;
      if (call.getMatchState() != EMatchState.MATCHED && !call.isChanged()) {
        unmatchedCalls.add(call);
      }
    }

    return unmatchedCalls;
  }

  public static Collection<RawJump> getUnmatchedIncomingJumps(
      final Diff diff,
      final RawFlowGraph priFlowgraph,
      final RawFlowGraph secFlowgraph,
      final RawBasicBlock basicblock) {
    final Collection<RawJump> unmatchedJumps = new ArrayList<>();

    final BasicBlockMatchData tarBasicblockMatch =
        MatchesGetter.getBasicBlockMatch(diff, basicblock);

    if (tarBasicblockMatch == null) {
      return getIncomingJumps(basicblock);
    }

    RawFlowGraph otherFlowgraph;
    otherFlowgraph = basicblock.getSide() == ESide.PRIMARY ? secFlowgraph : priFlowgraph;

    final Collection<RawJump> jumps = getIncomingJumps(basicblock);

    for (final RawJump jump : jumps) {
      final RawBasicBlock srcBasicblock = jump.getSource();

      final BasicBlockMatchData srcBasicblockMatch =
          MatchesGetter.getBasicBlockMatch(diff, srcBasicblock);

      if (srcBasicblockMatch != null) {
        IAddress otherSrcAddr = null;
        IAddress otherTarAddr = null;

        if (basicblock.getSide() == ESide.PRIMARY) {
          otherTarAddr = tarBasicblockMatch.getIAddress(ESide.SECONDARY);
          otherSrcAddr = srcBasicblockMatch.getIAddress(ESide.SECONDARY);
        } else {
          otherTarAddr = tarBasicblockMatch.getIAddress(ESide.PRIMARY);
          otherSrcAddr = srcBasicblockMatch.getIAddress(ESide.PRIMARY);
        }

        if (otherFlowgraph.getJump(otherSrcAddr, otherTarAddr) == null) {
          unmatchedJumps.add(jump);
        }
      } else {
        unmatchedJumps.add(jump);
      }
    }

    return unmatchedJumps;
  }

  public static List<RawCall> getUnmatchedOutgoingCalls(
      final Diff diff, final RawBasicBlock basicblock) {
    final List<RawCall> unmatchedOutgoingCalls = new ArrayList<>();

    final List<RawCall> outgoingCalls = getOutgoingCalls(diff, basicblock);

    for (final RawCall call : outgoingCalls) {
      if (call.getMatchState() != EMatchState.MATCHED && !call.isChanged()) {
        unmatchedOutgoingCalls.add(call);
      }
    }

    return unmatchedOutgoingCalls;
  }

  public static List<RawCall> getUnmatchedOutgoingCalls(final RawFunction function) {
    final List<RawCall> unmatchedCalls = new ArrayList<>();

    for (final SingleViewEdge<? extends SingleViewNode> outgoingCall :
        function.getOutgoingEdges()) {
      @SuppressWarnings("unchecked")
      final RawCall call = (RawCall) outgoingCall;
      if (call.getMatchState() != EMatchState.MATCHED && !call.isChanged()) {
        unmatchedCalls.add(call);
      }
    }

    return unmatchedCalls;
  }

  public static Collection<RawJump> getUnmatchedOutgoingJumps(
      final Diff diff,
      final RawFlowGraph priFlowgraph,
      final RawFlowGraph secFlowgraph,
      final RawBasicBlock basicblock) {
    final Collection<RawJump> unmatchedJumps = new ArrayList<>();

    final BasicBlockMatchData srcBasicblockMatch =
        MatchesGetter.getBasicBlockMatch(diff, basicblock);

    if (srcBasicblockMatch == null) {
      return getOutgoingJumps(basicblock);
    }

    RawFlowGraph otherFlowgraph;
    otherFlowgraph = basicblock.getSide() == ESide.PRIMARY ? secFlowgraph : priFlowgraph;

    final Collection<RawJump> jumps = getOutgoingJumps(basicblock);

    for (final RawJump jump : jumps) {
      final RawBasicBlock tarBasicblock = jump.getTarget();

      final BasicBlockMatchData tarBasicblockMatch =
          MatchesGetter.getBasicBlockMatch(diff, tarBasicblock);

      if (tarBasicblockMatch != null) {
        IAddress otherSrcAddr = null;
        IAddress otherTarAddr = null;

        if (basicblock.getSide() == ESide.PRIMARY) {
          otherSrcAddr = srcBasicblockMatch.getIAddress(ESide.SECONDARY);
          otherTarAddr = tarBasicblockMatch.getIAddress(ESide.SECONDARY);
        } else {
          otherSrcAddr = srcBasicblockMatch.getIAddress(ESide.PRIMARY);
          otherTarAddr = tarBasicblockMatch.getIAddress(ESide.PRIMARY);
        }

        if (otherFlowgraph.getJump(otherSrcAddr, otherTarAddr) == null) {
          unmatchedJumps.add(jump);
        }
      } else {
        unmatchedJumps.add(jump);
      }
    }

    return unmatchedJumps;
  }
}
