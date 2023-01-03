// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.project.rawflowgraph;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.graph.edges.CombinedViewEdge;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import java.util.List;

public class RawCombinedFlowGraph<
        NodeType extends RawCombinedBasicBlock, EdgeType extends RawCombinedJump<NodeType>>
    extends MutableDirectedGraph<NodeType, EdgeType> {
  private final RawFlowGraph primaryFlowgraph;
  private final RawFlowGraph secondaryFlowgraph;

  public RawCombinedFlowGraph(
      final List<NodeType> nodes,
      final List<EdgeType> edges,
      final RawFlowGraph primaryFlowgraph,
      final RawFlowGraph secondaryFlowgraph) {
    super(nodes, edges);

    if (primaryFlowgraph == null && secondaryFlowgraph == null) {
      throw new IllegalArgumentException("Primary and secondary flow graphs cannot both be null.");
    }

    this.primaryFlowgraph = primaryFlowgraph;
    this.secondaryFlowgraph = secondaryFlowgraph;
  }

  public EMatchState getMatchState() {
    if (secondaryFlowgraph == null) {
      return EMatchState.PRIMARY_UNMATCHED;
    }

    if (primaryFlowgraph == null) {
      return EMatchState.SECONDRAY_UNMATCHED;
    }

    return EMatchState.MATCHED;
  }

  public IAddress getPrimaryAddress() {
    if (primaryFlowgraph == null) {
      return null;
    }

    return primaryFlowgraph.getAddress();
  }

  public RawFlowGraph getPrimaryFlowgraph() {
    return primaryFlowgraph;
  }

  public String getPrimaryName() {
    if (primaryFlowgraph == null) {
      return "";
    }

    return primaryFlowgraph.getName();
  }

  public IAddress getSecondaryAddress() {
    if (secondaryFlowgraph == null) {
      return null;
    }

    return secondaryFlowgraph.getAddress();
  }

  public RawFlowGraph getSecondaryFlowgraph() {
    return secondaryFlowgraph;
  }

  public String getSecondaryName() {
    if (secondaryFlowgraph == null) {
      return "";
    }

    return secondaryFlowgraph.getName();
  }

  @Override
  public void removeNode(final NodeType node) {
    // removes node from nodes parent's children list
    // removes node's incoming edges from parent's outgoing edge list
    for (final CombinedViewEdge<? extends CombinedViewNode> edge : node.getIncomingEdges()) {
      CombinedViewNode.unlink(edge.getSource(), edge.getTarget());
      edge.getSource().removeOutgoingEdge(edge);
      edge.getTarget().removeIncomingEdge(edge);
    }

    // removes node from nodes child's parent list
    // removes node's outgoing edges from child's incoming edge list
    for (final CombinedViewEdge<? extends CombinedViewNode> edge : node.getOutgoingEdges()) {
      CombinedViewNode.unlink(edge.getSource(), edge.getTarget());
      edge.getTarget().removeIncomingEdge(edge);
      edge.getSource().removeOutgoingEdge(edge);
    }

    // removes node from node list
    // removes all incoming and outgoing edges of this node from edge list
    // removes entry from node to edges map
    super.removeNode(node);
  }
}
