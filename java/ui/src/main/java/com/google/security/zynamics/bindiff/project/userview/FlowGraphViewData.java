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

package com.google.security.zynamics.bindiff.project.userview;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.EViewType;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class FlowGraphViewData extends ViewData {
  private final RawFlowGraph primaryRawGraph;
  private final RawFlowGraph secondaryRawGraph;

  private final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      combinedRawFlowgraph;

  public FlowGraphViewData(
      final RawFlowGraph primaryRawFlowgraph,
      final RawFlowGraph secondaryRawFlowgraph,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedRawFlowgraph,
      final GraphsContainer graphs,
      final String viewName,
      final EViewType viewType) {
    super(graphs, viewName, viewType);

    Preconditions.checkArgument(
        primaryRawFlowgraph != null || secondaryRawFlowgraph != null,
        "Raw primary and secondary flow graphs cannot both be null");
    this.combinedRawFlowgraph = Preconditions.checkNotNull(combinedRawFlowgraph);
    this.primaryRawGraph = primaryRawFlowgraph;
    this.secondaryRawGraph = secondaryRawFlowgraph;
  }

  @Override
  public IAddress getAddress(final ESide side) {
    if (side == ESide.PRIMARY) {
      if (primaryRawGraph == null) {
        return null;
      }

      return primaryRawGraph.getAddress();
    }

    if (secondaryRawGraph == null) {
      return null;
    }

    return secondaryRawGraph.getAddress();
  }

  public RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      getCombinedRawGraph() {
    return combinedRawFlowgraph;
  }

  public String getFunctionName(final ESide side) {
    if (side == ESide.PRIMARY) {
      if (primaryRawGraph == null) {
        return null;
      }

      return primaryRawGraph.getName();
    }

    if (secondaryRawGraph == null) {
      return null;
    }

    return secondaryRawGraph.getName();
  }

  @Override
  public RawFlowGraph getRawGraph(final ESide side) {
    return side == ESide.PRIMARY ? primaryRawGraph : secondaryRawGraph;
  }

  @Override
  public boolean isCallgraphView() {
    return false;
  }

  public boolean isChangedOnlyInstructions() {
    final RawFunction function =
        getGraphs().getDiff().getCallGraph(ESide.PRIMARY).getFunction(getAddress(ESide.PRIMARY));
    return function != null && function.isChangedInstructionsOnlyMatch();
  }

  public boolean isChangedStructural() {
    final RawFunction function =
        getGraphs().getDiff().getCallGraph(ESide.PRIMARY).getFunction(getAddress(ESide.PRIMARY));
    return function != null && function.isChangedStructuralMatch();
  }

  @Override
  public boolean isFlowgraphView() {
    return true;
  }

  public boolean isMatched() {
    final RawFunction function =
        getGraphs().getDiff().getCallGraph(ESide.PRIMARY).getFunction(getAddress(ESide.PRIMARY));
    return function != null && function.getMatchedFunction() != null;
  }

  public boolean isMatchedIdentical() {
    final RawFunction function =
        getGraphs().getDiff().getCallGraph(ESide.PRIMARY).getFunction(getAddress(ESide.PRIMARY));
    return function != null && function.isIdenticalMatch();
  }
}
