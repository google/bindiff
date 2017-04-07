// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CNodeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeExitState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;

import java.awt.event.MouseEvent;

/**
 * Describes the default actions which are executed as soon as the mouse cursor exits a node.
 */
public class CDefaultNodeExitAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeExitState<NodeType, EdgeType>> {
  /**
   * Clears the tool tip of a graph.
   * 
   * @param graph The graph whose tool tip is cleared.
   */
  protected void clearTooltip(final AbstractZyGraph<?, ?> graph) {
    graph.getView().setToolTipText(null);
  }

  /**
   * Removes highlighting from the edges attached to a node.
   * 
   * @param node The node that was exited.
   */
  protected void unhighlightEdges(final Node node) {
    CEdgeHighlighter.highlightEdgesOfNode(node, false);
  }

  /**
   * Removes highlighting from the node that was exited.
   * 
   * @param node The node that was exited.
   */
  protected void unhighlightNode(final Node node) {
    CNodeHighlighter.highlightNode(node, false);
  }

  @Override
  public void execute(final CNodeExitState<NodeType, EdgeType> state, final MouseEvent event) {
    clearTooltip(state.getGraph());

    if (state.getNode().getGraph() != null) {
      // It's possible that the node was removed from the graph.

      unhighlightNode(state.getNode());
      unhighlightEdges(state.getNode());
    }

    final NodeType node = state.getGraph().getNode(state.getNode());

    if (node != null) {
      for (final IZyEditModeListener<NodeType, EdgeType> listener : state.getStateFactory()
          .getListeners()) {
        // ESCA-JAVA0166: Catch Exception because we are calling a listener function
        try {
          listener.nodeLeft(node);
        } catch (final Exception exception) {
          // TODO: log this
        }
      }
    }


    state.getGraph().updateViews();
  }
}
