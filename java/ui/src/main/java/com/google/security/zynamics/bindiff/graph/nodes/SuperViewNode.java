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

package com.google.security.zynamics.bindiff.graph.nodes;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.edges.SuperViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SuperViewNode extends CViewNode<SuperViewEdge<? extends SuperViewNode>>
    implements IGraphNode<SuperViewNode> {
  private final List<SuperViewNode> children = new ArrayList<>();
  private final List<SuperViewNode> parents = new ArrayList<>();

  private final CombinedViewNode combinedNode;

  public SuperViewNode(final CombinedViewNode combinedNode) {
    super(-1, 0, 0, 0, 0, Color.WHITE, Color.BLACK, false, true);

    checkNotNull(combinedNode);

    this.combinedNode = combinedNode;
  }

  public static void link(final SuperViewNode parent, final SuperViewNode child) {
    parent.getChildren().add(child);
    child.getParents().add(parent);
  }

  public static void unlink(final SuperViewNode parent, final SuperViewNode child) {
    parent.getChildren().remove(child);
    child.getParents().remove(parent);
  }

  @Override
  public List<SuperViewNode> getChildren() {
    return children;
  }

  public CombinedViewNode getCombinedNode() {
    return combinedNode;
  }

  @Override
  public IGroupNode<?, ?> getParentGroup() {
    // Not necessary until folding is reintroduced. So far null is a valid return value.
    return null;
  }

  @Override
  public List<SuperViewNode> getParents() {
    return parents;
  }

  public void removeNode() {
    // removes node from nodes parent's children list
    // removes node's incoming edges from parent's outgoing edge list
    for (final SuperViewEdge<? extends SuperViewNode> edge : getIncomingEdges()) {
      SuperViewNode.unlink(edge.getSource(), edge.getTarget());
      edge.getSource().removeOutgoingEdge(edge);
      edge.getTarget().removeIncomingEdge(edge);
    }

    // removes node from nodes child's parent list
    // removes node's outgoing edges from child's incoming edge list
    for (final SuperViewEdge<? extends SuperViewNode> edge : getOutgoingEdges()) {
      SuperViewNode.unlink(edge.getSource(), edge.getTarget());
      edge.getSource().removeOutgoingEdge(edge);
      edge.getTarget().removeIncomingEdge(edge);
    }
  }
}
