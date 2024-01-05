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

package com.google.security.zynamics.bindiff.graph.synchronizer;

import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import java.util.HashSet;
import java.util.Set;
import y.base.Edge;
import y.base.EdgeCursor;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeRealizer;

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
