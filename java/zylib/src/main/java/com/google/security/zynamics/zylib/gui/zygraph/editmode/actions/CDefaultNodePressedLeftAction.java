// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodePressedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.event.MouseEvent;


public class CDefaultNodePressedLeftAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodePressedLeftState<NodeType, EdgeType>> {
  @Override
  public void execute(final CNodePressedLeftState<NodeType, EdgeType> state, final MouseEvent event) {
    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();

    final NodeType draggedNode = graph.getNode(state.getNode());

    if (draggedNode != null) {
      final ZyLabelContent labelContent = draggedNode.getRealizer().getNodeContent();

      if (graph.getEditMode().getLabelEventHandler().isActiveLabel(labelContent)) {
        CEditNodeHelper.setCaretStart(graph, state.getNode(), event);
        CEditNodeHelper.setCaretEnd(graph, state.getNode(), event);
      } else {
        CEditNodeHelper.removeCaret(graph);
      }
    } else {
      CEditNodeHelper.removeCaret(graph);
    }
  }
}
