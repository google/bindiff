package com.google.security.zynamics.bindiff.graph.nodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.base.Node;

import java.util.ArrayList;
import java.util.List;

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

    Preconditions.checkNotNull(superDiffNode);

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
