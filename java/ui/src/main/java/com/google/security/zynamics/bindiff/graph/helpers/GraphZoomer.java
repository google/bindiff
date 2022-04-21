// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.graph.helpers;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.eventhandlers.GraphLayoutEventHandler;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ZoomHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.SwingUtilities;
import y.view.Graph2DView;

public class GraphZoomer {
  public static final double ZOOM_OUT_FACTOR = 0.95;

  private static void fitContent(final Graph2DView view) {
    view.fitContent();
    view.setZoom(view.getZoom() * GraphZoomer.ZOOM_OUT_FACTOR);
  }

  private static void zoomToArea(final BinDiffGraph<?, ?> graph, final Rectangle2D area) {
    final Graph2DView view = graph.getView();

    final double oldZoom = view.getZoom();
    final Point2D oldCenter = view.getCenter();

    view.zoomToArea(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    view.setZoom(view.getZoom() * ZOOM_OUT_FACTOR);
    ZoomHelpers.keepZoomValid(view);

    final double newZoom = view.getZoom();
    final Point2D.Double newCenter =
        new Point2D.Double(view.getCenter().getX(), view.getCenter().getY());

    view.setZoom(oldZoom);
    view.setCenter(oldCenter.getX(), oldCenter.getY());

    GraphAnimator.zoomGraph(graph, newCenter, newZoom);

    view.getGraph2D().updateViews();
  }

  private static void zoomToArea(final SuperGraph graph, final Rectangle2D area) {
    GraphViewFitter.adoptSuperViewCanvasProperties(graph);

    final Graph2DView primaryView = graph.getPrimaryGraph().getView();
    final Graph2DView secondaryView = graph.getSecondaryGraph().getView();

    // primary
    final double priOldZoom = primaryView.getZoom();
    final Point2D priOldCenter = primaryView.getViewPoint2D();

    primaryView.zoomToArea(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    primaryView.setZoom(primaryView.getZoom() * ZOOM_OUT_FACTOR);
    ZoomHelpers.keepZoomValid(primaryView);

    final double priNewZoom = primaryView.getZoom();

    primaryView.setZoom(priOldZoom);
    primaryView.setViewPoint2D(priOldCenter.getX(), priOldCenter.getY());

    // secondary
    final double secOldZoom = secondaryView.getZoom();
    final Point2D secOldCenter = secondaryView.getViewPoint2D();

    secondaryView.zoomToArea(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    secondaryView.setZoom(secondaryView.getZoom() * ZOOM_OUT_FACTOR);
    ZoomHelpers.keepZoomValid(secondaryView);

    final double secNewZoom = secondaryView.getZoom();

    secondaryView.setZoom(secOldZoom);
    secondaryView.setViewPoint2D(secOldCenter.getX(), secOldCenter.getY());

    GraphAnimator.zoomGraph(graph, area, priNewZoom, secNewZoom);

    primaryView.getGraph2D().updateViews();
    secondaryView.getGraph2D().updateViews();
  }

  private static void zoomToNodes(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph, final Collection<ZyGraphNode<?>> nodes) {
    if (nodes.size() == 0) {
      return;
    }

    final Rectangle2D area = GraphHelpers.calculateBoundingBox(nodes);
    zoomToArea(graph, area);
  }

  public static void fitContent(final BinDiffGraph<?, ?> graph) {
    fitContent(graph.getView());
    graph.getGraph().updateViews();
  }

  public static void fitContent(final SuperGraph superGraph) {
    final GraphSettings settings = superGraph.getSettings();

    if (settings.isSync()) {
      superGraph.getView().fitContent();

      GraphViewFitter.adoptSuperViewCanvasProperties(superGraph);
      GraphViewFitter.fitSingleViewToSuperViewContent(superGraph);

    } else if (settings.getFocus() == ESide.PRIMARY) {
      fitContent(superGraph.getPrimaryGraph().getView());
    } else {
      fitContent(superGraph.getSecondaryGraph().getView());
    }

    superGraph.getPrimaryGraph().getGraph().updateViews();
    superGraph.getSecondaryGraph().getGraph().updateViews();
  }

  @SuppressWarnings("unchecked")
  public static void zoomToNode(final BinDiffGraph<?, ?> graph, final ZyGraphNode<?> node) {
    checkNotNull(graph);
    checkNotNull(node);

    if (!node.isVisible()) {
      GraphLayoutEventHandler.handleUnhideInvisibleNode(
          (BinDiffGraph<ZyGraphNode<?>, ?>) graph, node);
    }

    final Collection<ZyGraphNode<?>> nodes = new ArrayList<>();
    nodes.add(node);

    SwingUtilities.invokeLater(() -> zoomToNodes((BinDiffGraph<ZyGraphNode<?>, ?>) graph, nodes));
  }

  public static void zoomToNodes(
      final CombinedGraph graph, final Collection<CombinedDiffNode> nodes) {
    checkNotNull(graph);
    checkNotNull(nodes);

    for (final CombinedDiffNode node : nodes) {
      if (!node.isVisible()) {
        // In order to handle invisible nodes also, write something similar to
        // GraphLayoutEventHandler.handleUnhideInvisibleNode((BinDiffGraph<ZyGraphNode<?>,?>)graph,
        // node);
        throw new IllegalArgumentException(
            "This function does not handle invisible nodes. Each node must be visible.");
      }
    }

    if (nodes.size() == 0) {
      return;
    }

    final Rectangle2D area = GraphHelpers.calculateBoundingBox(nodes);
    zoomToArea(graph, area);
  }

  public static void zoomToNodes(final SuperGraph graph, final Collection<SuperDiffNode> nodes) {
    checkNotNull(graph);
    checkNotNull(nodes);

    for (final SuperDiffNode node : nodes) {
      if (!node.isVisible()) {
        // In order to handle invisible nodes also, write something similar to
        // GraphLayoutEventHandler.handleUnhideInvisibleNode((BinDiffGraph<ZyGraphNode<?>,?>)graph,
        // node);
        throw new IllegalArgumentException(
            "This function does not handle invisible nodes. Each node must be visible.");
      }
    }

    if (nodes.size() == 0) {
      return;
    }

    GraphViewFitter.adoptSuperViewCanvasProperties(graph);

    final Rectangle2D area = GraphHelpers.calculateBoundingBox(nodes);

    if (graph.getSettings().isSync()) {
      zoomToArea(graph, area);
    } else if (graph.getSettings().getFocus() == ESide.PRIMARY) {
      zoomToArea(graph.getPrimaryGraph(), area);
    } else {
      zoomToArea(graph.getSecondaryGraph(), area);
    }

    graph.getPrimaryGraph().getGraph().updateViews();
    graph.getSecondaryGraph().getGraph().updateViews();
  }
}
