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

package com.google.security.zynamics.bindiff.graph.searchers;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.helpers.GraphMover;
import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.Color;
import java.awt.Window;
import java.util.List;
import javax.swing.SwingUtilities;

public class GraphSearcherFunctions {
  private static void markResults(final SearchResult result, final boolean mark) {
    final Color borderColor =
        mark ? Colors.SEARCH_HIGHLIGHT_COLOR : result.getOriginalBorderColor();

    final SingleDiffNode diffNode = (SingleDiffNode) result.getObject();

    if (mark) {
      diffNode.setBackgroundColor(
          result.getLine(),
          result.getPosition(),
          result.getLength(),
          Colors.SEARCH_HIGHLIGHT_COLOR);
    } else {
      final int line = result.getLine();
      for (final CStyleRunData styleRun : result.getOriginalTextBackgroundStyleRun()) {
        Color color = styleRun.getColor();
        final ESide side = diffNode.getSide();

        if (color != null) {
          if (diffNode.isSelected()) {
            if (side == ESide.PRIMARY && Colors.PRIMARY_BASE.equals(color)
                || side == ESide.SECONDARY && Colors.SECONDARY_BASE.equals(color)) {
              color = color.darker();
            }
          } else {
            if (side == ESide.PRIMARY && Colors.PRIMARY_BASE.darker().equals(color)) {
              color = Colors.PRIMARY_BASE;
            } else if (side == ESide.SECONDARY && Colors.SECONDARY_BASE.equals(color)) {
              color = Colors.SECONDARY_BASE;
            }
          }
        }

        diffNode.setBackgroundColor(line, styleRun.getStart(), styleRun.getLength(), color);
      }
    }

    diffNode.getRealizer().getRealizer().setLineColor(borderColor);
    final CombinedDiffNode combinedDiffNode = diffNode.getCombinedDiffNode();

    final SingleDiffNode priNode = combinedDiffNode.getPrimaryDiffNode();
    final SingleDiffNode secNode = combinedDiffNode.getSecondaryDiffNode();

    combinedDiffNode.getRealizer().getRealizer().setLineColor(borderColor);
    if (priNode != null && secNode != null) {
      if (!priNode
          .getRealizer()
          .getRealizer()
          .getLineColor()
          .equals(secNode.getRealizer().getRealizer().getLineColor())) {
        combinedDiffNode.getRealizer().getRealizer().setLineColor(Colors.MIXED_BASE_COLOR.darker());
      }
    }
  }

  public static void clearResults(final GraphsContainer graphs) {
    for (final BinDiffGraph<?, ?> graph : graphs) {
      graph.getGraphSearcher().clearResults();
    }
  }

  public static boolean getHasChanged(final GraphsContainer graphs, final String searchString) {
    for (final BinDiffGraph<?, ?> graph : graphs) {
      if (graph.getGraphSearcher().getHasChanged(searchString)) {
        return true;
      }
    }

    return false;
  }

  public static void highlightResults(final List<SearchResult> results) {
    for (final SearchResult result : results) {
      markResults(result, true);
    }
  }

  public static void highlightSubObjectResults(final GraphsContainer graphs) {
    for (final BinDiffGraph<?, ?> graph : graphs) {
      if (!(graph instanceof SingleGraph)) {
        continue;
      }

      for (final SearchResult result : graph.getGraphSearcher().getSubObjectResults()) {
        markResults(result, true);
      }
    }
  }

  public static boolean isEmpty(final GraphsContainer graphs) {
    for (final BinDiffGraph<?, ?> graph : graphs) {
      if (!graph.getGraphSearcher().getObjectResults().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public static void iterateObjectResults(
      final GraphsContainer graphs, final boolean cycleBackwards, final boolean zoomToResult) {
    // Skip to the previous (CTRL+ENTER) or next (ENTER) search result. (SHIFT zooms to target)
    final BinDiffGraph<?, ?> graph = graphs.getFocusedGraph();
    final GraphSearcher graphSearcher = graph.getGraphSearcher();

    final Window parent = SwingUtilities.getWindowAncestor(graphs.getCombinedGraph().getView());

    if (cycleBackwards) {
      graphSearcher.previous();

      if (graphSearcher.isBeforeFirst()) {
        CMessageBox.showInformation(
            parent, "All search results were displayed. Going back to the last one.");
      }
    } else {
      graphSearcher.next();

      if (graphSearcher.isAfterLast()) {
        CMessageBox.showInformation(
            parent, "All search results were displayed. Going back to the first one.");
      }
    }

    final Object object = graphSearcher.current();

    if (object == null) {
      return;
    }

    jumpToResultObject(graph, object, zoomToResult);
  }

  public static void jumpToFirstResultObject(
      final BinDiffGraph<?, ?> graph, final boolean zoomToResult) {
    final List<Object> results = graph.getGraphSearcher().getObjectResults();

    if (!results.isEmpty()) {
      final Object object = results.get(0);

      jumpToResultObject(graph, object, zoomToResult);
    }
  }

  public static void jumpToResultObject(
      final BinDiffGraph<?, ?> graph, final Object object, final boolean zoomToResult) {
    if (object == null) {
      return;
    }

    if (object instanceof ZyGraphNode<?>) {
      BinDiffGraph<? extends ZyGraphNode<?>, ?> tempGraph = graph;
      Object tempObj = object;

      if (graph instanceof SuperGraph) {
        tempGraph = graph.getPrimaryGraph();
        tempObj = ((SuperDiffNode) object).getPrimaryDiffNode();

        if (tempObj == null) {
          tempGraph = graph.getSecondaryGraph();
          tempObj = ((SuperDiffNode) object).getSecondaryDiffNode();
        }
      }

      if (zoomToResult) {
        GraphZoomer.zoomToNode(tempGraph, (ZyGraphNode<?>) tempObj);
      } else {
        GraphMover.moveToNode(tempGraph, (ZyGraphNode<?>) tempObj);
      }
    }
  }

  public static void removeHighlighting(final List<SearchResult> results) {
    for (final SearchResult result : results) {
      removeSubObjectHighlighting(result);
    }
  }

  public static void removeSubObjectHighlighting(final SearchResult searchResult) {
    markResults(searchResult, false);
  }

  public static void search(final GraphsContainer graphs, final String searchString) {
    clearResults(graphs);

    for (final BinDiffGraph<?, ?> graph : graphs) {
      final List<? extends ZyGraphNode<?>> nodes = GraphHelpers.getNodes(graph);
      final List<? extends ZyGraphEdge<?, ?, ?>> edges = GraphHelpers.getEdges(graph);

      graph.getGraphSearcher().search(nodes, edges, searchString);
    }

    final List<Object> primaryObjects =
        graphs.getPrimaryGraph().getGraphSearcher().getObjectResults();
    final List<Object> secondaryObjects =
        graphs.getSecondaryGraph().getGraphSearcher().getObjectResults();

    final SuperGraph superGraph = graphs.getSuperGraph();
    final CombinedGraph combinedGraph = graphs.getCombinedGraph();

    ((SuperGraphSearcher) superGraph.getGraphSearcher())
        .setObjectResults(superGraph, primaryObjects, secondaryObjects);
    ((CombinedGraphSearcher) combinedGraph.getGraphSearcher())
        .setObjectResults(combinedGraph, primaryObjects, secondaryObjects);
  }
}
