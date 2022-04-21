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
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.sorters.SearchResultSorter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombinedGraphSearcher extends GraphSearcher {
  @Override
  public void search(
      final List<? extends ZyGraphNode<?>> nodes,
      final List<? extends ZyGraphEdge<?, ?, ?>> edges,
      final String searchString) {
    setLastSearchString(searchString);
  }

  public void setObjectResults(
      final CombinedGraph graph,
      final List<Object> primaryObjects,
      final List<Object> secondaryObjects) {
    final Set<Object> combinedObjects = new HashSet<>();

    final Set<Object> singleObjects = new HashSet<>();

    singleObjects.addAll(primaryObjects);
    singleObjects.addAll(secondaryObjects);

    for (final CombinedDiffNode combinedNode : graph.getNodes()) {
      final SingleDiffNode primaryDiffNode = combinedNode.getPrimaryDiffNode();

      if (singleObjects.contains(primaryDiffNode)) {
        combinedObjects.add(combinedNode);

        continue;
      }

      final SingleDiffNode secondaryDiffNode = combinedNode.getSecondaryDiffNode();

      if (singleObjects.contains(secondaryDiffNode)) {
        combinedObjects.add(combinedNode);
      }
    }

    for (final CombinedDiffEdge combinedEdge : graph.getEdges()) {
      final SingleDiffEdge primaryDiffEdge = combinedEdge.getPrimaryDiffEdge();

      if (singleObjects.contains(primaryDiffEdge)) {
        combinedObjects.add(combinedEdge);

        continue;
      }

      final SingleDiffEdge secondaryDiffEdge = combinedEdge.getSecondaryDiffEdge();

      if (singleObjects.contains(secondaryDiffEdge)) {
        combinedObjects.add(combinedEdge);
      }
    }

    final List<Object> sortedList =
        SearchResultSorter.getSortedList(combinedObjects, ESide.PRIMARY);
    setObjectResult(sortedList);
  }
}
