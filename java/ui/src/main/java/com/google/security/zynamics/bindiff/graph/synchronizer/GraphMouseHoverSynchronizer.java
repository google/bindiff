package com.google.security.zynamics.bindiff.graph.synchronizer;

import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;

import y.base.Edge;
import y.base.EdgeCursor;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeRealizer;

import java.util.HashSet;
import java.util.Set;

public class GraphMouseHoverSynchronizer {
  private static void highlightEdge(final Edge edge) {
    final EdgeRealizer realizer = ((Graph2D) edge.getGraph()).getRealizer(edge);
    final LineType lineType = realizer.getLineType();

    if (lineType == LineType.LINE_2) {
      realizer.setLineType(LineType.LINE_5);
    } else if (lineType == LineType.DASHED_2) {
      realizer.setLineType(LineType.DASHED_5);
    } else if (lineType == LineType.DOTTED_2) {
      realizer.setLineType(LineType.DOTTED_5);
    } else if (lineType == LineType.LINE_5) {
      realizer.setLineType(LineType.LINE_2);
    } else if (lineType == LineType.DASHED_5) {
      realizer.setLineType(LineType.DASHED_2);
    } else if (lineType == LineType.DOTTED_5) {
      realizer.setLineType(LineType.DOTTED_2);
    }
  }

  public static void adoptHoveredNodeState(final SingleGraph graph, final SingleDiffNode node) {
    if (graph.getSettings().isSync()) {
      final SingleDiffNode otherSideNode = node.getOtherSideDiffNode();

      if (otherSideNode != null) {
        final NodeRealizer referenceRealizer = node.getRealizer().getRealizer();
        final NodeRealizer otherSideRealizer = otherSideNode.getRealizer().getRealizer();

        otherSideRealizer.setLineType(referenceRealizer.getLineType());

        // recursive edges are iterated twice (once as incoming and once as outgoing edge!)
        final Set<Edge> edgeSet = new HashSet<>();
        for (final EdgeCursor ec = otherSideNode.getNode().edges(); ec.ok(); ec.next()) {
          if (!edgeSet.contains(ec.edge())) {
            highlightEdge(ec.edge());
          }

          edgeSet.add(ec.edge());
        }
      }

      graph.getSecondaryGraph().updateViews();
      graph.getPrimaryGraph().updateViews();
    }
  }
}
