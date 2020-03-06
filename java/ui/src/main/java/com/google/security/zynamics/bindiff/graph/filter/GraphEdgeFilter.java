// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
