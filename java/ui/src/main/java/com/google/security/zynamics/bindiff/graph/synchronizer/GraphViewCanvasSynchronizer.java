// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.graph.synchronizer;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.listeners.GraphViewsListenerManager;

import y.view.Graph2DView;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

public class GraphViewCanvasSynchronizer {
  private static void addListener(
      final GraphViewsListenerManager listenerManager, final SingleGraph referenceGraph) {
    if (referenceGraph.getSide() == ESide.PRIMARY) {
      listenerManager.addViewCanvasListener(referenceGraph.getSecondaryGraph());
    } else {
      listenerManager.addViewCanvasListener(referenceGraph.getPrimaryGraph());
    }
  }

  private static void adoptViewPoint(
      final SingleGraph referenceGraph,
      final Point2D oldViewPoint,
      final boolean suppressUpdateGraph) {
    final SingleGraph targetGraph = getTargetGraph(referenceGraph);

    final Graph2DView referenceView = referenceGraph.getView();
    final Graph2DView targetView = targetGraph.getView();

    final Point2D refViewPoint = referenceView.getViewPoint2D();
    final double dx = oldViewPoint.getX() - refViewPoint.getX();
    final double dy = oldViewPoint.getY() - refViewPoint.getY();

    final Point2D tarViewPoint = targetView.getViewPoint2D();
    targetView.setViewPoint2D(tarViewPoint.getX() - dx, tarViewPoint.getY() - dy);

    // Note: Leads to flickering when graph layout morphing of single views only!
    // Note: Can not be seen always, but often for a few milliseconds there is a strongly zoomed
    // basic block flicking!
    // Note: Can't be commented because otherwise the other single graph is not updated when the
    // graph is moved by dragging the background.
    if (!suppressUpdateGraph) {
      targetView.getGraph2D().updateViews();
    }
  }

  private static void adoptZoom(final SingleGraph referenceGraph) {
    final SingleGraph targetGraph = getTargetGraph(referenceGraph);
    final SuperGraph superGraph = referenceGraph.getSuperGraph();

    adoptSuperWorldRect(superGraph);

    targetGraph
        .getView()
        .focusView(referenceGraph.getView().getZoom(), referenceGraph.getView().getCenter(), false);
  }

  private static SingleGraph getTargetGraph(final SingleGraph referenceGraph) {
    if (referenceGraph == referenceGraph.getPrimaryGraph()) {
      return referenceGraph.getSecondaryGraph();
    }

    return referenceGraph.getPrimaryGraph();
  }

  private static void removeListener(
      final GraphViewsListenerManager listenerManager, final SingleGraph referenceGraph) {
    if (referenceGraph.getSide() == ESide.PRIMARY) {
      listenerManager.removeViewCanvasListener(referenceGraph.getSecondaryGraph());
    } else {
      listenerManager.removeViewCanvasListener(referenceGraph.getPrimaryGraph());
    }
  }

  public static void adoptSuperWorldRect(final SuperGraph superGraph) {
    if (superGraph.getSettings().isSync()) {

      final Rectangle superRect = superGraph.getView().getWorldRect();

      final SingleGraph primaryGraph = superGraph.getPrimaryGraph();
      final SingleGraph secondaryGraph = superGraph.getSecondaryGraph();

      primaryGraph
          .getView()
          .setWorldRect(superRect.x, superRect.y, superRect.width, superRect.height);
      secondaryGraph
          .getView()
          .setWorldRect(superRect.x, superRect.y, superRect.width, superRect.height);
    }
  }

  public static void adoptViewPoint(
      final GraphViewsListenerManager listenerManager,
      final SingleGraph referenceGraph,
      final Point2D oldViewPoint,
      final boolean suppressUpdateGraph) {
    if (referenceGraph.getSettings().isSync()) {
      removeListener(listenerManager, referenceGraph);

      adoptViewPoint(referenceGraph, oldViewPoint, suppressUpdateGraph);

      addListener(listenerManager, referenceGraph);
    }
  }

  public static void adoptZoom(
      final GraphViewsListenerManager listenerManager, final SingleGraph referenceGraph) {
    if (referenceGraph.getSettings().isSync()) {
      removeListener(listenerManager, referenceGraph);

      adoptZoom(referenceGraph);

      addListener(listenerManager, referenceGraph);
    }
  }
}
