// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.AbstractZyGraphSettings;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;

import y.base.Edge;
import y.view.EdgeRealizer;
import y.view.NodeRealizer;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class CEdgeClickHandler {
  /**
   * Zooms to the source or target of an edge depending on what is farther away from the visible
   * part of the graph.
   * 
   * @param graph The graph the edge belongs to.
   * @param edge The edge that provides the potential zoom targets.
   */
  private static void zoomEdgeNode(final AbstractZyGraph<?, ?> graph, final Edge edge,
      final double mouseX, final double mouseY) {
    assert edge != null;

    final AbstractZyGraphSettings settings = graph.getSettings();
    final boolean animate = settings.getLayoutSettings().getAnimateLayout();

    final EdgeRealizer realizer = graph.getGraph().getRealizer(edge);

    final NodeRealizer sourceRealizer = graph.getGraph().getRealizer(edge.source());
    final NodeRealizer targetRealizer = graph.getGraph().getRealizer(edge.target());

    final double srcPortX = realizer.getSourcePort().getX(sourceRealizer);
    final double srcPortY = realizer.getSourcePort().getY(sourceRealizer);
    final double tarPortX = realizer.getSourcePort().getX(targetRealizer);
    final double tarPortY = realizer.getSourcePort().getY(targetRealizer);

    final double srcLengthA = Math.abs(srcPortX - mouseX);
    final double srcHeightB = Math.abs(srcPortY - mouseY);
    final double tarLengthA = Math.abs(tarPortX - mouseX);
    final double tarHeightB = Math.abs(tarPortY - mouseY);

    final double srcLengthC = Math.sqrt(Math.pow(srcLengthA, 2) + Math.pow(srcHeightB, 2));
    final double tarLengthC = Math.sqrt(Math.pow(tarLengthA, 2) + Math.pow(tarHeightB, 2));

    if (srcLengthC > tarLengthC) {
      final Point2D.Double center =
          new Point2D.Double(sourceRealizer.getCenterX(), sourceRealizer.getCenterY());
      graph.getView().focusView(graph.getView().getZoom(), center, animate);
    } else {
      final Point2D.Double center =
          new Point2D.Double(targetRealizer.getCenterX(), targetRealizer.getCenterY());
      graph.getView().focusView(graph.getView().getZoom(), center, animate);
    }
  }

  /**
   * Handles clicks on edges.
   * 
   * @param graph The graph the edge belongs to.
   * @param edge The edge that was clicked on.
   * @param event Description of the click event.
   */
  public static <EdgeType extends ZyGraphEdge<?, ?, ?>> void handleEdgeClicks(
      final AbstractZyGraph<?, ?> graph, final EdgeType edge, final MouseEvent event) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");
    Preconditions.checkNotNull(edge, "Error: Edge argument can not be null");
    Preconditions.checkNotNull(event, "Error: Event argument can not be null");

    if ((event.getButton() == MouseEvent.BUTTON1) && event.isShiftDown()) {
      // Shift-Click => Select edge

      graph.getGraph().setSelected(edge.getEdge(), !edge.isSelected());
    } else if ((event.getButton() == MouseEvent.BUTTON1) && !event.isShiftDown()) {
      if (edge.getSource() != edge.getTarget()) {
        // When the user clicks on an edge, the graph scrolls to either
        // the source node or the target node of the edge, depending on
        // their distance from the visible part of the graph.

        final double x = graph.getView().toWorldCoordX(event.getX());
        final double y = graph.getView().toWorldCoordY(event.getY());

        zoomEdgeNode(graph, edge.getEdge(), x, y);
      }
    }
  }
}
