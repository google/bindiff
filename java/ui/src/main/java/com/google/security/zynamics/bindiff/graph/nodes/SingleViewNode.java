package com.google.security.zynamics.bindiff.graph.nodes;

import com.google.common.base.Preconditions;
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
    Preconditions.checkNotNull(addr);

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
