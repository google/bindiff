// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.graph.layout;

import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import y.base.Graph;
import y.layout.GraphLayout;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.LayoutMorpher;

public class SuperLayoutMorpher extends LayoutMorpher {
  private final Graph2D superGraph;

  private Graph2DView view;

  public SuperLayoutMorpher(
      final Graph2DView graphView, final GraphLayout graphLayout, final Graph2D superGraph) {
    super(graphView, graphLayout);

    this.superGraph = superGraph;
  }

  @Override
  protected Rectangle2D calcBoundingBox(
      final Graph graph, final GraphLayout graphLayout, final Rectangle2D rect) {
    super.calcBoundingBox(superGraph, graphLayout, rect);

    // backup current values
    final Rectangle oldVisibleRect = view.getVisibleRect();
    final double oldZoom = view.getZoom();

    // Note: If the listeners are not removed here a flickering appears on graph layout morphing of
    // single views!
    // Note: Can not be seen always, but often for a few milliseconds there is a strongly zoomed
    // basic block flicking!
    final PropertyChangeListener[] listeners = view.getPropertyChangeListeners();
    for (final PropertyChangeListener listener : listeners) {
      view.removePropertyChangeListener(listener);
    }

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

    for (final PropertyChangeListener listener : listeners) {
      view.addPropertyChangeListener(listener);
    }

    return rect;
  }

  /** Overwritten to store the specified view for later queries. */
  @Override
  protected void initialize(final Graph2DView view, final GraphLayout layout) {
    super.initialize(view, layout);

    this.view = view;
  }
}
