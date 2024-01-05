// Copyright 2011-2024 Google LLC
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

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.edges.CombinedViewEdge;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class CombinedViewNode
    extends CViewNode<CombinedViewEdge<? extends CombinedViewNode>>
    implements IGraphNode<CombinedViewNode> {
  private final List<CombinedViewNode> children = new ArrayList<>();
  private final List<CombinedViewNode> parents = new ArrayList<>();

  public CombinedViewNode() {
    super(-1, 0, 0, 0, 0, Color.WHITE, Color.BLACK, false, true);
  }

  public static void link(final CombinedViewNode parent, final CombinedViewNode child) {
    parent.getChildren().add(child);
    child.getParents().add(parent);
  }

  public static void unlink(final CombinedViewNode parent, final CombinedViewNode child) {
    parent.getChildren().remove(child);
    child.getParents().remove(parent);
  }

  public abstract IAddress getAddress(ESide side);

  @Override
  public List<CombinedViewNode> getChildren() {
    return children;
  }

  @Override
  public List<CombinedViewEdge<? extends CombinedViewNode>> getIncomingEdges() {
    return super.getIncomingEdges();
  }

  public EMatchState getMatchState() {
    if (getRawNode(ESide.PRIMARY) == null) {
      return EMatchState.SECONDRAY_UNMATCHED;
    }
    if (getRawNode(ESide.SECONDARY) == null) {
      return EMatchState.PRIMARY_UNMATCHED;
    }

    return EMatchState.MATCHED;
  }

  @Override
  public List<CombinedViewEdge<? extends CombinedViewNode>> getOutgoingEdges() {
    return super.getOutgoingEdges();
  }

  @Override
  public IGroupNode<?, ?> getParentGroup() {
    // Not necessary until folding is reintroduced. So far null is a valid return value.
    return null;
  }

  @Override
  public List<CombinedViewNode> getParents() {
    return parents;
  }

  public abstract SingleViewNode getRawNode(ESide side);

  // public void removeChild(final CombinedViewNode child)
  // {
  // children.remove(child);
  // }
  //
  // public void removeParent(final CombinedViewNode parent)
  // {
  // parents.remove(parent);
  // }
}
