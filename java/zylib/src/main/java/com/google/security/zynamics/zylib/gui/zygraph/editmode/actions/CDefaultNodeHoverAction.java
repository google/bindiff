// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeHoverState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.event.MouseEvent;


/**
 * Describes the default actions which are executed as soon as the mouse hovers over a node.
 */
public class CDefaultNodeHoverAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeHoverState<NodeType, EdgeType>> {
  @Override
  public void execute(final CNodeHoverState<NodeType, EdgeType> state, final MouseEvent event) {
    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();
    CMouseCursorHelper.setDefaultCursor(graph);

    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    final NodeType node = graph.getNode(state.getNode());

    if (node != null) // node == null => Proximity Node
    {
      for (final IZyEditModeListener<NodeType, EdgeType> listener : state.getStateFactory()
          .getListeners()) {
        try {
          listener.nodeHovered(node, x, y);
        } catch (final Exception exception) {
          // TODO: (timkornau): implement logging here.
        }
      }
    }
  }
}
