// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeExitState;

import y.base.Edge;
import y.view.Graph2D;

import java.awt.event.MouseEvent;

public class CDefaultEdgeExitAction implements IStateAction<CEdgeExitState> {
  protected void clearTooltip(final AbstractZyGraph<?, ?> graph) {
    graph.getView().setToolTipText(null);
  }

  protected void unhighlightEdge(final Edge edge) {
    CEdgeHighlighter.highlightEdge(((Graph2D) edge.getGraph()).getRealizer(edge), false);
  }

  @Override
  public void execute(final CEdgeExitState state, final MouseEvent event) {
    clearTooltip(state.getGraph());

    // This check is necessary because it is possible for edges to be deleted
    // while the mouse is hovering over them. In that case unhighlighting
    // a deleted edge causes problems.
    if (state.getEdge().getGraph() != null) {
      unhighlightEdge(state.getEdge());
    }

    state.getGraph().updateViews();
  }
}
