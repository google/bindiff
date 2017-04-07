package com.google.security.zynamics.bindiff.graph.nodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.base.Node;

import java.util.ArrayList;
import java.util.List;

public class SuperDiffNode extends ZyGraphNode<SuperViewNode> implements IGraphNode<SuperDiffNode> {
  private final SingleDiffNode primaryDiffNode;

  private final SingleDiffNode secondaryDiffNode;

  private CombinedDiffNode combinedDiffNode;

  private final List<SuperDiffNode> parents = new ArrayList<>();

  private final List<SuperDiffNode> children = new ArrayList<>();

  public SuperDiffNode(
      final Node node,
      final IZyNodeRealizer realizer,
      final SuperViewNode superNode,
      final SingleDiffNode primaryDiffNode,
      final SingleDiffNode secondaryDiffNode) {
    super(node, realizer, superNode);
    Preconditions.checkArgument(
        primaryDiffNode != null || secondaryDiffNode != null,
        "Primary diff node and secondary diff node cannot be both be null.");

    this.primaryDiffNode = primaryDiffNode;
    this.secondaryDiffNode = secondaryDiffNode;
  }

  public static void link(final SuperDiffNode sourceNode, final SuperDiffNode targetNode) {
    sourceNode.children.add(targetNode);
    targetNode.parents.add(sourceNode);
  }

  public static void unlink(final SuperDiffNode sourceNode, final SuperDiffNode targetNode) {
    sourceNode.children.remove(targetNode);
    targetNode.parents.remove(sourceNode);
  }

  @Override
  public SuperViewNode getRawNode() {
    return super.getRawNode();
  }

  @Override
  public List<SuperDiffNode> getChildren() {
    return new ArrayList<>(children);
  }

  public CombinedDiffNode getCombinedDiffNode() {
    return combinedDiffNode;
  }

  public CombinedViewNode getCombinedRawNode() {
    return combinedDiffNode != null ? combinedDiffNode.getRawNode() : null;
  }

  @Override
  public List<SuperDiffNode> getParents() {
    return new ArrayList<>(parents);
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

  public void setCombinedDiffNode(final CombinedDiffNode combinedDiffNode) {
    Preconditions.checkNotNull(combinedDiffNode);

    this.combinedDiffNode = combinedDiffNode;
  }
}
