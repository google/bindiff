package com.google.security.zynamics.bindiff.graph.editmode.actions;

import com.google.security.zynamics.bindiff.graph.editmode.helpers.EditCombinedNodeHelper;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodePressedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.event.MouseEvent;

public class CombinedNodePressedLeftAction<
        NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodePressedLeftState<NodeType, EdgeType>> {
  @Override
  public void execute(
      final CNodePressedLeftState<NodeType, EdgeType> state, final MouseEvent event) {
    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();

    final NodeType draggedNode = graph.getNode(state.getNode());

    if (draggedNode != null) {
      final ZyLabelContent labelContent = draggedNode.getRealizer().getNodeContent();

      if (graph.getEditMode().getLabelEventHandler().isActiveLabel(labelContent)) {
        EditCombinedNodeHelper.setCaretStart(graph, state.getNode(), event);
        EditCombinedNodeHelper.setCaretEnd(graph, state.getNode(), event);
        return;
      }
    }
    EditCombinedNodeHelper.removeCaret(graph);
  }
}
