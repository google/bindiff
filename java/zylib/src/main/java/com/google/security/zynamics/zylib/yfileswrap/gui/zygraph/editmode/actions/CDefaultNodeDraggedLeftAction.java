// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers.CMouseCursorHelper;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraphLayeredRenderer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEditNodeHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CNodeMover;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeDraggedLeftState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;
import y.view.Bend;

import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class CDefaultNodeDraggedLeftAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeDraggedLeftState<NodeType, EdgeType>> {
  protected void moveToFront(final ZyGraphLayeredRenderer<?> renderer, final Node node) {
    renderer.bringNodeToFront(node);
  }

  @Override
  public void execute(final CNodeDraggedLeftState<NodeType, EdgeType> state, final MouseEvent event) {
    CMouseCursorHelper.setHandCursor(state.getGraph());

    moveToFront((ZyGraphLayeredRenderer<?>) state.getGraph().getView().getGraph2DRenderer(),
        state.getNode());

    // 1. The dragged node is always moved
    // 2. The selected nodes are only moved if the dragged node is selected too

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();
    final Set<Bend> movedBends = new HashSet<Bend>();
    final NodeType draggedNode = graph.getNode(state.getNode());
    final ZyLabelContent labelContent = draggedNode.getRealizer().getNodeContent();

    if (graph.getEditMode().getLabelEventHandler().isActiveLabel(labelContent)) {
      CEditNodeHelper.setCaretEnd(graph, state.getNode(), event);
      CEditNodeHelper.select(graph, state.getNode(), event);
    } else if (draggedNode.isSelected()) {
      for (final NodeType n : graph.getSelectedNodes()) {
        CNodeMover.moveNode(graph, n, state.getDistanceX(), state.getDistanceY(), movedBends);
      }
    } else {
      CNodeMover.moveNode(graph, draggedNode, state.getDistanceX(), state.getDistanceY(),
          movedBends);
    }

    graph.updateViews();
  }
}
