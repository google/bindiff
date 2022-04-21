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

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class SingleViewNode extends CViewNode<SingleViewEdge<? extends SingleViewNode>>
    implements IGraphNode<SingleViewNode> {
  private final IAddress address;

  private final List<SingleViewNode> children = new ArrayList<>();
  private final List<SingleViewNode> parents = new ArrayList<>();

  public SingleViewNode(
      final IAddress addr,
      final int id,
      final double x,
      final double y,
      final Color color,
      final Color borderColor,
      final boolean selected,
      final boolean visible) {
    super(id, x, y, 0, 0, color, borderColor, selected, visible);
    checkNotNull(addr);

    address = addr;
  }

  public static void link(final SingleViewNode parent, final SingleViewNode child) {
    parent.getChildren().add(child);
    child.getParents().add(parent);
  }

  public static void unlink(final SingleViewNode parent, final SingleViewNode child) {
    parent.getChildren().remove(child);
    child.getParents().remove(parent);
  }

  public IAddress getAddress() {
    return address;
  }

  @Override
  public List<SingleViewNode> getChildren() {
    return children;
  }

  @Override
  public List<SingleViewEdge<? extends SingleViewNode>> getIncomingEdges() {
    return super.getIncomingEdges();
  }

  public abstract EMatchState getMatchState();

  @Override
  public List<SingleViewEdge<? extends SingleViewNode>> getOutgoingEdges() {
    return super.getOutgoingEdges();
  }

  @Override
  public IGroupNode<?, ?> getParentGroup() {
    // Not necessary until node folding is introduced. So long null is a valid return value.
    return null;
  }

  @Override
  public List<SingleViewNode> getParents() {
    return parents;
  }

  public abstract ESide getSide();
}
