// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CEdgeLabelExitState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.view.EdgeRealizer;

import java.awt.event.MouseEvent;

public class CDefaultEdgeLabelExitAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CEdgeLabelExitState<NodeType, EdgeType>> {
  /**
   * Removes highlighting from the edges attached to a edge label.
   * 
   * @param edge the EdgeRealizer of the edge
   */
  protected void unhighlightEdges(final EdgeRealizer edge) {
    CEdgeHighlighter.highlightEdge(edge, false);
  }

  @Override
  public void execute(final CEdgeLabelExitState<NodeType, EdgeType> state, final MouseEvent event) {
    unhighlightEdges(state.getLabel().getOwner());

    if (state.getLabel() != null) {
      unhighlightEdges(state.getLabel().getOwner());

      for (final IZyEditModeListener<NodeType, EdgeType> listener : state.getStateFactory()
          .getListeners()) {
        try {
          listener.edgeLabelLeft(state.getLabel());
        } catch (final Exception exception) {
          // TODO: (timkornau): implement logging.
        }
      }
    }

    state.getGraph().updateViews();

  }

}
