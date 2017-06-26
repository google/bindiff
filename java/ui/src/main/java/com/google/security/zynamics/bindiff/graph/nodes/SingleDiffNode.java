package com.google.security.zynamics.bindiff.graph.nodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.base.Node;

import java.util.ArrayList;
import java.util.List;

public class SingleDiffNode extends ZyGraphNode<SingleViewNode>
    implements IGraphNode<SingleDiffNode> {
  private CombinedDiffNode combinedDiffNode;

  private SuperDiffNode superDiffNode;

  private SingleDiffNode otherSideDiffNode;

  private final ESide side;

  private final List<SingleDiffNode> parents = new ArrayList<>();

  private final List<SingleDiffNode> children = new ArrayList<>();

  public SingleDiffNode(
      final Node node,
      final IZyNodeRealizer realizer,
      final SingleViewNode rawNode,
      final ESide side) {
    super(node, realizer, rawNode);
    this.side = Preconditions.checkNotNull(side);
  }

  public static void link(final SingleDiffNode sourceNode, final SingleDiffNode targetNode) {
    sourceNode.children.add(targetNode);
    targetNode.parents.add(sourceNode);
  }

  public static void unlink(final SingleDiffNode sourceNode, final SingleDiffNode targetNode) {
    sourceNode.children.remove(targetNode);
    targetNode.parents.remove(sourceNode);
  }

  @Override
  public List<SingleDiffNode> getChildren() {
    return new ArrayList<>(children);
  }

  public CombinedDiffNode getCombinedDiffNode() {
    return combinedDiffNode;
  }

  public CombinedViewNode getCombinedRawNode() {
    return combinedDiffNode.getRawNode();
  }

  public SingleDiffNode getOtherSideDiffNode() {
    return otherSideDiffNode;
  }

  public SingleViewNode getOtherSideRawNode() {
    return otherSideDiffNode != null ? otherSideDiffNode.getRawNode() : null;
  }

  @Override
  public List<? extends SingleDiffNode> getParents() {
    return new ArrayList<>(parents);
  }

  @Override
  public SingleViewNode getRawNode() {
    return super.getRawNode();
  }

  public ESide getSide() {
    return side;
  }

  public SuperDiffNode getSuperDiffNode() {
    return superDiffNode;
  }

  public SuperViewNode getSuperRawNode() {
    return superDiffNode.getRawNode();
  }

  public void setCombinedDiffNode(final CombinedDiffNode combinedDiffNode) {
    Preconditions.checkNotNull(combinedDiffNode);

    this.combinedDiffNode = combinedDiffNode;
    superDiffNode = combinedDiffNode.getSuperDiffNode();
    otherSideDiffNode =
        side == ESide.PRIMARY
            ? combinedDiffNode.getSecondaryDiffNode()
            : combinedDiffNode.getPrimaryDiffNode();
  }
}