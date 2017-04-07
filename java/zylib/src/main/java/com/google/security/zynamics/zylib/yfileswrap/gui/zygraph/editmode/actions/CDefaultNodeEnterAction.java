// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.actions;

import com.google.security.zynamics.zylib.gui.zygraph.editmode.IStateAction;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.IZyEditModeListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CEdgeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CNodeHighlighter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CTooltipUpdater;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states.CNodeEnterState;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;

import java.awt.event.MouseEvent;

/**
 * Describes the default actions which are executed as soon as the mouse cursor enters a node.
 */
public class CDefaultNodeEnterAction<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IStateAction<CNodeEnterState<NodeType, EdgeType>> {
  /**
   * Highlights the edges that are attached to the node that is entered.
   * 
   * @param node The node that is entered.
   */
  protected void highlightEdges(final Node node) {
    CEdgeHighlighter.highlightEdgesOfNode(node, true);
  }

  /**
   * Highlights the entered node.
   * 
   * @param node The node that is entered.
   */
  protected void highlightNode(final Node node) {
    CNodeHighlighter.highlightNode(node, true);
  }

  /**
   * Updates the tooltip that is shown in the graph depending on the entered node.
   * 
   * @param graph The graph that contains the node.
   * @param node The node that is entered.
   */
  protected void updateTooltip(final AbstractZyGraph<?, ?> graph, final Node node) {
    CTooltipUpdater.updateNodeTooltip(graph, node);
  }

  @Override
  public void execute(final CNodeEnterState<NodeType, EdgeType> state, final MouseEvent event) {
    highlightNode(state.getNode());
    highlightEdges(state.getNode());
    updateTooltip(state.getGraph(), state.getNode());

    final AbstractZyGraph<NodeType, EdgeType> graph = state.getGraph();

    final NodeType node = graph.getNode(state.getNode());

    for (final IZyEditModeListener<NodeType, EdgeType> listener : state.getStateFactory()
        .getListeners()) {
      try {
        listener.nodeEntered(node, event);
      } catch (final Exception exception) {
        // TODO: log this
      }
    }

    graph.updateViews();
  }
}
