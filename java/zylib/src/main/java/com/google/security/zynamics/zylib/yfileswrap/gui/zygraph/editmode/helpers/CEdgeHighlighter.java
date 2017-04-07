// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;

/**
 * Helper class for edge highlighting in mouse-over situations.
 */
public final class CEdgeHighlighter {
  /**
   * Highlights a single edge realizer.
   * 
   * @param realizer The realizer to highlight.
   * @param state True, to highlight the realizer. False, to unhighlight it.
   */
  public static void highlightEdge(final EdgeRealizer realizer, final boolean state) {
    if (state) {
      if (realizer.getLineType() == LineType.LINE_2) {
        realizer.setLineType(LineType.LINE_5);
      } else if (realizer.getLineType() == LineType.DOTTED_2) {
        realizer.setLineType(LineType.DOTTED_5);
      } else if (realizer.getLineType() == LineType.DASHED_2) {
        realizer.setLineType(LineType.DASHED_5);
      } else if (realizer.getLineType() == LineType.DASHED_DOTTED_2) {
        realizer.setLineType(LineType.DASHED_DOTTED_5);
      }
    } else {
      if (realizer == null) {
        return;
      }

      if (realizer.getLineType() == LineType.LINE_5) {
        realizer.setLineType(LineType.LINE_2);
      } else if (realizer.getLineType() == LineType.DOTTED_5) {
        realizer.setLineType(LineType.DOTTED_2);
      } else if (realizer.getLineType() == LineType.DASHED_5) {
        realizer.setLineType(LineType.DASHED_2);
      } else if (realizer.getLineType() == LineType.DASHED_DOTTED_5) {
        realizer.setLineType(LineType.DASHED_DOTTED_2);
      }
    }
  }

  /**
   * Highlights all edges of a node.
   * 
   * @param node The node whose edges are highlighted.
   * @param highlight True to add highlighting to the edges. False to remove it.
   */
  public static void highlightEdgesOfNode(final Node node, final boolean highlight) {
    final EdgeCursor edges = node.edges();

    int edgeCount = node.degree();

    for (Edge edge = edges.edge(); edgeCount > 0; edgeCount--) {
      final EdgeRealizer edgeRealizer = ((Graph2D) node.getGraph()).getRealizer(edge);

      highlightEdge(edgeRealizer, highlight);

      edges.cyclicNext();
      edge = edges.edge();
    }
  }
}
