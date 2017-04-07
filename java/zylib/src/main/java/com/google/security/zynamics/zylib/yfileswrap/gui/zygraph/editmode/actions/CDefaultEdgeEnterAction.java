// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CTooltipUpdater;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeEnterState;

import y.base.Edge;
import y.view.Graph2D;

import java.awt.event.MouseEvent;

public class CDefaultEdgeEnterAction implements IStateAction<CEdgeEnterState> {
  protected void highlightEdge(final Edge edge) {
    CEdgeHighlighter.highlightEdge(((Graph2D) edge.getGraph()).getRealizer(edge), true);
  }

  protected void updateTooltip(final AbstractZyGraph<?, ?> graph, final Edge edge) {
    CTooltipUpdater.updateEdgeTooltip(graph, edge);
  }

  @Override
  public void execute(final CEdgeEnterState state, final MouseEvent event) {
    highlightEdge(state.getEdge());
    updateTooltip(state.getGraph(), state.getEdge());

    state.getGraph().updateViews();
  }
}
