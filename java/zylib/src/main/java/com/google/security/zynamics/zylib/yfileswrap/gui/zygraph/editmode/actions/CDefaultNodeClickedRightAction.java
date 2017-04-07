// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeClickedRightState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ProximityHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

import y.base.Node;

import java.awt.event.MouseEvent;

public class CDefaultNodeClickedRightAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeClickedRightState<NodeType, EdgeType>> {
  @Override
  public void execute(final CNodeClickedRightState<NodeType, EdgeType> state, final MouseEvent event) {
    CMouseCursorHelper.setDefaultCursor(state.getGraph());

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();

    final Node yNode = state.getNode();

    final NodeType node = graph.getNode(yNode);

    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    if (graph.getEditMode().getLabelEventHandler().isActive()
        && graph.getEditMode().getLabelEventHandler().hasEmptySelection()) {
      CEditNodeHelper.removeCaret(graph);
    }

    if (node == null) {
      // Proximity node was clicked

      final ZyProximityNode<?> proximityNode =
          ProximityHelper.getProximityNode(graph.getGraph(), yNode);

      for (final IZyEditModeListener<NodeType, EdgeType> listener : state.getStateFactory()
          .getListeners()) {
        try {
          listener.proximityBrowserNodeClicked(proximityNode, event, x, y);
        } catch (final Exception exception) {
          // TODO: (timkornau): implement logging here.
        }
      }
    } else {
      for (final IZyEditModeListener<NodeType, EdgeType> listener : state.getStateFactory()
          .getListeners()) {
        try {
          listener.nodeClicked(node, event, x, y);
        } catch (final Exception exception) {
          // TODO: (timkornau): implement logging here.
        }
      }
    }
  }
}
