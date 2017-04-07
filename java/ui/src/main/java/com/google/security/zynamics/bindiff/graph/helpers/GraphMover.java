package com.google.security.zynamics.bindiff.graph.helpers;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.eventhandlers.GraphLayoutEventHandler;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Edge;
import y.base.Node;
import y.view.EdgeRealizer;
import y.view.NodeRealizer;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

public class GraphMover {
  public static void moveToEdgeNode(
      final BinDiffGraph<?, ?> graph, final Edge edge, final double mouseX, final double mouseY) {
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

    Point2D.Double center;
    if (srcLengthC > tarLengthC) {
      center = new Point2D.Double(sourceRealizer.getCenterX(), sourceRealizer.getCenterY());
    } else {
      center = new Point2D.Double(targetRealizer.getCenterX(), targetRealizer.getCenterY());
    }

    GraphAnimator.moveGraph(graph, center);
  }

  @SuppressWarnings("unchecked")
  public static void moveToNode(
      final BinDiffGraph<? extends ZyGraphNode<?>, ?> graph, final ZyGraphNode<?> zyNode) {
    if (!zyNode.isVisible()) {
      GraphLayoutEventHandler.handleUnhideInvisibleNode(
          (BinDiffGraph<ZyGraphNode<?>, ?>) graph, zyNode);
    }

    final Collection<ZyGraphNode<?>> tempList = new ArrayList<>();
    tempList.add(zyNode);

    final Node node = zyNode.getNode();

    final NodeRealizer realizer = graph.getGraph().getRealizer(node);
    final Point2D.Double center = new Point2D.Double(realizer.getCenterX(), realizer.getCenterY());

    GraphAnimator.moveGraph(graph, center);
  }
}
