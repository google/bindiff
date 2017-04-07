// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;


public class ProximityHelper {
  public static ZyProximityNode<?> getProximityNode(final Graph2D graph, final Node node) {
    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getRealizer(node);

    return (ZyProximityNode<?>) realizer.getUserData().getNode();
  }

  public static boolean isProximityEdge(final Graph2D graph, final Edge edge) {
    return isProximityNode(graph, edge.source()) || isProximityNode(graph, edge.target());
  }

  public static boolean isProximityNode(final Graph2D graph, final Node node) {
    final IZyNodeRealizer realizer = (IZyNodeRealizer) graph.getRealizer(node);

    return realizer.getUserData().getNode() instanceof ZyProximityNode<?>;
  }

}
