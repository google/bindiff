package com.google.security.zynamics.bindiff.graph.filter;

import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;

import java.util.ArrayList;
import java.util.Collection;

/** Helper functions for filtering edges by visibility state, etc. */
public class GraphEdgeFilter {
  /**
   * Returns a <code>Collection</code> of all visible edges of the specified graph.
   *
   * @param graph the graph that contains the edges
   * @return a <code>Collection</code> containing all visible edges of the graph. May be empty.
   */
  public static <EdgeType extends ZyGraphEdge<?, ?, ?>> Collection<EdgeType> filterInvisibleEdges(
      final AbstractZyGraph<?, EdgeType> graph) {
    final Collection<EdgeType> edges = new ArrayList<>();
    graph.iterateEdges(
        new IEdgeCallback<EdgeType>() {
          @Override
          public IterationMode nextEdge(EdgeType edge) {
            if (edge.isVisible()) {
              edges.add(edge);
            }
            return IterationMode.CONTINUE;
          }
        });
    return edges;
  }
}
