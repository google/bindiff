package com.google.security.zynamics.bindiff.graph.editmode;

import com.google.security.zynamics.bindiff.graph.editmode.actions.SingleGraphNodeDraggedLeftAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeDraggedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class SingleGraphActionFactory<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    extends InvertMoveViewPortGraphActionFactory<NodeType, EdgeType> {
  @Override
  public IStateAction<CNodeDraggedLeftState<NodeType, EdgeType>> createNodeDraggedLeftAction() {
    return new SingleGraphNodeDraggedLeftAction<>();
  }
}
