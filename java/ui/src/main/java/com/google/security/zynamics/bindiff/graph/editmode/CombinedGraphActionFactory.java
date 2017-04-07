package com.google.security.zynamics.bindiff.graph.editmode;

import com.google.security.zynamics.bindiff.graph.editmode.actions.CombinedNodeClickedMiddleAction;
import com.google.security.zynamics.bindiff.graph.editmode.actions.CombinedNodeDraggedLeftAction;
import com.google.security.zynamics.bindiff.graph.editmode.actions.CombinedNodePressedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeClickedMiddleState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeDraggedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodePressedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class CombinedGraphActionFactory<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    extends InvertMoveViewPortGraphActionFactory<NodeType, EdgeType> {
  @Override
  public IStateAction<CNodeClickedMiddleState> createNodeClickedMiddleAction() {
    return new CombinedNodeClickedMiddleAction();
  }

  @Override
  public IStateAction<CNodeDraggedLeftState<NodeType, EdgeType>> createNodeDraggedLeftAction() {
    return new CombinedNodeDraggedLeftAction<>();
  }

  @Override
  public IStateAction<CNodePressedLeftState<NodeType, EdgeType>> createNodePressedLeftAction() {
    return new CombinedNodePressedLeftAction<>();
  }
}
