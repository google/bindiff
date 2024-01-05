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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ZoomHelpers;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import y.geom.YRectangle;
import y.view.EdgeLabel;
import y.view.NodeRealizer;

public class ZoomFunctions {
  public static <EdgeType extends ZyGraphEdge<?, ?, ?>> void zoomToEdgeLabel(
      final AbstractZyGraph<?, EdgeType> graph, final EdgeType edge) {
    final EdgeLabel label = edge.getRealizer().getLabel();

    final YRectangle labelBounds = label.getBox();
    final Rectangle viewBounds = graph.getView().getBounds();

    final double widthZoom = viewBounds.getWidth() / labelBounds.getWidth();
    final double heightZoom = viewBounds.getHeight() / labelBounds.getHeight();

    final double oldZoom = graph.getView().getZoom();
    graph.getView().setZoom(0.8 * Math.min(widthZoom, heightZoom));
    ZoomHelpers.keepZoomValid(graph.getView());
    final double newZoom = graph.getView().getZoom();
    graph.getView().setZoom(oldZoom);

    final Point2D newCenter =
        new Point2D.Double(
            labelBounds.getX() + (0.5 * labelBounds.getWidth()),
            labelBounds.getY() + (0.5 * labelBounds.getHeight()));

    graph.getView().focusView(newZoom, newCenter, true);

    graph.updateViews();
  }

  /**
   * Zooms to a single node.
   *
   * @param node The node in question.
   */
  public static <NodeType extends ZyGraphNode<?>> void zoomToNode(
      final AbstractZyGraph<NodeType, ?> graph, final NodeType node) {
    Preconditions.checkNotNull(node, "Error: Node argument can't be null");

    final NodeRealizer realizer = graph.getGraph().getRealizer(node.getNode());

    Preconditions.checkNotNull(realizer, "Error: Node does not belong to the graph");

    if (!node.isVisible()) {
      graph.showNode(node, true);
    }

    final double oldZoom = graph.getView().getZoom();
    final Point2D oldViewPoint = graph.getView().getViewPoint2D();

    graph
        .getView()
        .zoomToArea(
            realizer.getCenterX() - realizer.getWidth(),
            realizer.getCenterY() - realizer.getHeight(),
            realizer.getWidth() * 2,
            realizer.getHeight() * 2);
    ZoomHelpers.keepZoomValid(graph.getView());

    final double newZoom = graph.getView().getZoom();
    final Point2D newCenter = graph.getView().getCenter();

    graph.getView().setZoom(oldZoom);
    graph.getView().setViewPoint((int) oldViewPoint.getX(), (int) oldViewPoint.getY());

    graph
        .getView()
        .focusView(newZoom, newCenter, graph.getSettings().getLayoutSettings().getAnimateLayout());

    graph.updateViews();
  }

  public static <NodeType extends ZyGraphNode<?>> void zoomToNode(
      final AbstractZyGraph<NodeType, ?> graph,
      final NodeType node,
      final int line,
      final boolean animate) {
    Preconditions.checkNotNull(node, "Error: Node argument can't be null");

    final IZyNodeRealizer realizer = node.getRealizer();

    Preconditions.checkNotNull(realizer, "Error: Node does not belong to the graph");

    final double offset = realizer.getNodeContent().getLineHeight() * line;

    final Point2D oldViewPoint = graph.getView().getViewPoint2D();

    graph
        .getView()
        .setCenter(
            realizer.getCenterX(), (realizer.getCenterY() - (realizer.getHeight() / 2)) + offset);

    if (animate) {
      final Point2D newCenter = graph.getView().getCenter();

      graph.getView().setViewPoint((int) oldViewPoint.getX(), (int) oldViewPoint.getY());
      graph
          .getView()
          .focusView(
              graph.getView().getZoom(),
              newCenter,
              graph.getSettings().getLayoutSettings().getAnimateLayout());
    }

    graph.updateViews();
  }

  /**
   * Zooms the graph so far that all nodes in the list are visible.
   *
   * @param nodes List of nodes that should be displayed as big as possible.
   */
  public static <NodeType extends ZyGraphNode<?>> void zoomToNodes(
      final AbstractZyGraph<NodeType, ?> graph, final Collection<NodeType> nodes) {
    Preconditions.checkNotNull(nodes, "Error: nodes argument can not be null");

    if (nodes.size() == 0) {
      return;
    }

    final double oldZoom = graph.getView().getZoom();
    final Point2D oldViewPoint = graph.getView().getViewPoint2D();

    final Rectangle2D box = GraphHelpers.calculateBoundingBox(nodes);
    graph.getView().zoomToArea(box.getX(), box.getY(), box.getWidth(), box.getHeight());
    ZoomHelpers.keepZoomValid(graph.getView());
    graph.zoomOut();

    final double newZoom = graph.getView().getZoom();
    final Point2D newCenter = graph.getView().getCenter();

    graph.getView().setZoom(oldZoom);
    graph.getView().setViewPoint((int) oldViewPoint.getX(), (int) oldViewPoint.getY());

    graph
        .getView()
        .focusView(newZoom, newCenter, graph.getSettings().getLayoutSettings().getAnimateLayout());

    graph.updateViews();
  }

  /** Zooms the graph so far that all nodes are non-hidden nodes are visible on the screen. */
  public static void zoomToScreen(final AbstractZyGraph<?, ?> graph) {
    graph.getView().fitContent();

    ZoomHelpers.keepZoomValid(graph.getView());

    graph.updateViews();
  }
}
