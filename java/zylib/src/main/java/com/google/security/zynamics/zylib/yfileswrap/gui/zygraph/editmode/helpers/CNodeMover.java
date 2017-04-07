// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Edge;
import y.base.EdgeCursor;
import y.view.Bend;
import y.view.BendCursor;

import java.util.Set;

public class CNodeMover {
  public static boolean isDraggedFar(final double x1, final double y1, final double x2,
      final double y2) {
    final double xdiff = x1 - x2;
    final double ydiff = y1 - y2;

    return (Math.abs(xdiff) > 15) || (Math.abs(ydiff) > 15);
  }

  public static void moveNode(final AbstractZyGraph<?, ?> graph, final ZyGraphNode<?> node,
      final double xdist, final double ydist, final Set<Bend> movedBends) {
    graph.getGraph().getRealizer(node.getNode()).moveBy(xdist, ydist);

    for (final EdgeCursor cursor = node.getNode().edges(); cursor.ok(); cursor.next()) {
      final Edge edge = cursor.edge();

      for (final BendCursor bendCursor = graph.getGraph().getRealizer(edge).bends(); bendCursor
          .ok(); bendCursor.next()) {
        final Bend bend = bendCursor.bend();

        if (movedBends.contains(bend)) {
          continue;
        }

        bend.moveBy(xdist, ydist);

        movedBends.add(bend);
      }
    }
  }
}
