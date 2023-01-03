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

package com.google.security.zynamics.bindiff.graph.nodes;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import java.util.ArrayList;
import java.util.List;
import y.base.Node;

public class CombinedDiffNode extends ZyGraphNode<CombinedViewNode>
    implements IGraphNode<CombinedDiffNode> {
  private final SuperDiffNode superDiffNode;

  private final SingleDiffNode primaryDiffNode;

  private final SingleDiffNode secondaryDiffNode;

  private final List<CombinedDiffNode> parents = new ArrayList<>();

  private final List<CombinedDiffNode> children = new ArrayList<>();

  public CombinedDiffNode(
      final Node node,
      final IZyNodeRealizer realizer,
      final CombinedViewNode rawNode,
      final SuperDiffNode superDiffNode) {
    super(node, realizer, rawNode);

    checkNotNull(superDiffNode);

    this.superDiffNode = superDiffNode;
    primaryDiffNode = superDiffNode.getPrimaryDiffNode();
    secondaryDiffNode = superDiffNode.getSecondaryDiffNode();
  }

  public static void link(final CombinedDiffNode sourceNode, final CombinedDiffNode targetNode) {
    sourceNode.children.add(targetNode);
    targetNode.parents.add(sourceNode);
  }

  public static void unlink(final CombinedDiffNode sourceNode, final CombinedDiffNode targetNode) {
    sourceNode.children.remove(targetNode);
    targetNode.parents.remove(sourceNode);
  }

  @Override
  public List<CombinedDiffNode> getChildren() {
    return new ArrayList<>(children);
  }

  @Override
  public List<CombinedDiffNode> getParents() {
    return new ArrayList<>(parents);
  }

  @Override
  public CombinedViewNode getRawNode() {
    return super.getRawNode();
  }

  public SingleDiffNode getPrimaryDiffNode() {
    return primaryDiffNode;
  }

  public SingleViewNode getPrimaryRawNode() {
    return primaryDiffNode != null ? primaryDiffNode.getRawNode() : null;
  }

  public SingleDiffNode getSecondaryDiffNode() {
    return secondaryDiffNode;
  }

  public SingleViewNode getSecondaryRawNode() {
    return secondaryDiffNode != null ? secondaryDiffNode.getRawNode() : null;
  }

  public SuperDiffNode getSuperDiffNode() {
    return superDiffNode;
  }

  public SuperViewNode getSuperRawNode() {
    return superDiffNode.getRawNode();
  }
}
