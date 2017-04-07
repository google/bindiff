package com.google.security.zynamics.bindiff.graph.layout;

import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;

import y.base.Graph;
import y.layout.GraphLayout;
import y.view.Graph2DView;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public class LayoutMorpher extends y.view.LayoutMorpher {
  private Graph2DView view;

  public LayoutMorpher(final Graph2DView graphView, final GraphLayout graphLayout) {
    super(graphView, graphLayout);
  }

  @Override
  protected Rectangle2D calcBoundingBox(
      final Graph graph, final GraphLayout graphLayout, final Rectangle2D rect) {
    super.calcBoundingBox(graph, graphLayout, rect);

    // Backup current values.
    final Rectangle oldVisibleRect = view.getVisibleRect();
    final double oldZoom = view.getZoom();

    view.fitRectangle(rect.getBounds());
    view.setZoom(view.getZoom() * GraphZoomer.ZOOM_OUT_FACTOR);

    final Rectangle newVisibleRect = view.getVisibleRect();
    rect.setFrame(
        newVisibleRect.getX(),
        newVisibleRect.getY(),
        newVisibleRect.getWidth(),
        newVisibleRect.getHeight());

    // restore old state
    view.fitRectangle(oldVisibleRect);
    view.setZoom(oldZoom);

    return rect;
  }

  /** Overwritten to store the specified view for later queries. */
  @Override
  protected void initialize(final Graph2DView view, final GraphLayout layout) {
    super.initialize(view, layout);

    this.view = view;
  }
}
