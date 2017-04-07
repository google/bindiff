package com.google.security.zynamics.bindiff.graph.editmode.actions;

import com.google.security.zynamics.bindiff.graph.helpers.GraphElementMover;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraphLayeredRenderer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions.CDefaultNodeDraggedLeftAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeDraggedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;
import y.base.Node;

public class SingleGraphNodeDraggedLeftAction<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeDraggedLeftState<NodeType, EdgeType>> {
  protected void moveToFront(final ZyGraphLayeredRenderer<?> renderer, final Node node) {
    renderer.bringNodeToFront(node);
  }

  @Override
  public void execute(
      final CNodeDraggedLeftState<NodeType, EdgeType> state, final MouseEvent event) {
    new CDefaultNodeDraggedLeftAction<NodeType, EdgeType>().execute(state, event);

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();
    final NodeType draggedNode = graph.getNode(state.getNode());
    final ZyLabelContent labelContent = draggedNode.getRealizer().getNodeContent();

    if (!graph.getEditMode().getLabelEventHandler().isActiveLabel(labelContent)) {
      final Node node = state.getNode();
      moveToFront((ZyGraphLayeredRenderer<?>) graph.getView().getGraph2DRenderer(), node);

      GraphElementMover.moveNodes(graph, node, state.getDistanceX(), state.getDistanceY());
    }
  }
}
