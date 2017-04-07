// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyInfoEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeClickedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.event.MouseEvent;


public class CDefaultEdgeClickedRightAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CEdgeClickedRightState<NodeType, EdgeType>> {
  @Override
  public void execute(final CEdgeClickedRightState<NodeType, EdgeType> state, final MouseEvent event) {
    CMouseCursorHelper.setDefaultCursor(state.getGraph());

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();

    @SuppressWarnings("unchecked")
    final EdgeType edgeT = (EdgeType) state.getEdge();
    if (edgeT instanceof ZyInfoEdge) {
      return;
    }

    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    for (final IZyEditModeListener<NodeType, EdgeType> listener : state.getStateFactory()
        .getListeners()) {
      try {
        listener.edgeClicked(edgeT, event, x, y);
      } catch (final Exception exception) {
        // TODO: (timkornau): implement a useful logging.
      }
    }
  }
}
