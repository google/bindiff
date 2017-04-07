// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.ISelectableNode;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IViewableNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import y.geom.YRectangle;
import y.view.NodeRealizer;

public class MoveFunctions {
  public static <EdgeType extends ZyGraphEdge<?, ?, ?>> void centerEdgeLable(
      final AbstractZyGraph<?, EdgeType> graph, final EdgeType edge) {
    Preconditions.checkNotNull(edge, "Error: Edge can't be null.");

    if (edge.isVisible()) {
      final YRectangle box = edge.getRealizer().getLabel().getBox();
      final double x = box.x + (box.width / 2);
      final double y = box.y + (box.height / 2);

      graph.getView().focusView(graph.getView().getZoom(), new Point2D.Double(x, y),
          graph.getSettings().getLayoutSettings().getAnimateLayout());

      graph.updateViews();
    } else {
      throw new IllegalStateException("Error: Edge does not belong to graph.");
    }
  }

  /**
   * Centers the graph on a single node.
   *
   * <p>Note that the node must be visible. Otherwise the centering operation has no effect.
   *
   * @param node The node in question.
   */
  public static <
          NodeType extends ZyGraphNode<?> & ISelectableNode & IViewableNode,
          EdgeType extends ZyGraphEdge<?, ?, ?>>
      void centerNode(final AbstractZyGraph<NodeType, ?> graph, final NodeType node) {
    Preconditions.checkNotNull(node, "Error: Node argument can't be null");

    final NodeRealizer realizer = graph.getGraph().getRealizer(node.getNode());

    if (realizer.isVisible()) {
      graph.getView().focusView(graph.getView().getZoom(),
          new Point2D.Double(realizer.getCenterX(), realizer.getCenterY()),
          graph.getSettings().getLayoutSettings().getAnimateLayout());
      graph.updateViews();
    }
  }

  /**
   * Centers the screen on a list of nodes.
   *
   * @param nodes The list of nodes in question.
   */
  public static <
          NodeType extends ZyGraphNode<?> & ISelectableNode & IViewableNode,
          EdgeType extends ZyGraphEdge<?, ?, ?>>
      void centerNodes(final AbstractZyGraph<NodeType, ?> graph, final Set<NodeType> nodes) {
    Preconditions.checkNotNull(nodes, "Error: Nodes argument is null");
    Preconditions.checkArgument(!nodes.isEmpty(), "Error: Nodes argument is empty");

    // To center all nodes, we calculate the bounding box that includes all nodes.
    final Rectangle2D box = GraphHelpers.calculateBoundingBox(nodes);

    // Center the graph to make sure all nodes are visible.
    graph.getView().setCenter(box.getX() + (box.getWidth() / 2.),
        box.getY() + (box.getHeight() / 2.));

    graph.updateViews();
  }

  public static void pan(final AbstractZyGraph<?, ?> graph, double dx, double dy) {
    // scrolling moving of graph by dx and dy, could be renamed to move(dx, dy)

    final double amount = (50.0 * 1.0) / graph.getView().getZoom();
    dx *= amount;
    dy *= amount;
    final Point2D.Double p = (Point2D.Double) graph.getView().getCenter();

    double newXCenter = p.getX() + dx;
    double newYCenter = p.getY() + dy;

    if (dx < 0) {
      dx = (int) Math.floor(dx);
    } else {
      dx = (int) Math.ceil(dx) + 2;
    }

    if (dy < 0) {
      dy = (int) Math.floor(dy);
    } else {
      dy = (int) Math.ceil(dy) + 2;
    }

    final Rectangle worldRect = graph.getView().getWorldRect();
    final Rectangle visibleRect = graph.getView().getVisibleRect();

    if ((visibleRect.x + (int) dx) < worldRect.x) {
      newXCenter += worldRect.x - (visibleRect.x + (int) dx);
    } else if ((visibleRect.x + visibleRect.width + (int) dx) > (worldRect.x + worldRect.width)) {
      newXCenter -=
          (visibleRect.x + visibleRect.width + (int) dx) - (worldRect.x + worldRect.width);
    }

    if ((visibleRect.y + (int) dy) < worldRect.y) {
      newYCenter += worldRect.y - (visibleRect.y + (int) dy);
    } else if ((visibleRect.y + visibleRect.height + (int) dy) > (worldRect.y + worldRect.height)) {
      newYCenter -=
          (visibleRect.y + visibleRect.height + (int) dy) - (worldRect.y + worldRect.height);
    }

    graph.getView().setCenter(newXCenter, newYCenter);

    graph.updateViews();
  }
}
