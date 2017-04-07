package com.google.security.zynamics.bindiff.graph.searchers;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.sorters.SearchResultSorter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SuperGraphSearcher extends GraphSearcher {
  @Override
  public void search(
      final List<? extends ZyGraphNode<?>> nodes,
      final List<? extends ZyGraphEdge<?, ?, ?>> edges,
      final String searchString) {
    setLastSearchString(searchString);
  }

  public void setObjectResults(
      final SuperGraph graph,
      final List<Object> primaryObjects,
      final List<Object> secondaryObjects) {
    final Set<Object> superObjects = new HashSet<>();

    final Set<Object> singleObjects = new HashSet<>();

    singleObjects.addAll(primaryObjects);
    singleObjects.addAll(secondaryObjects);

    for (final SuperDiffNode superNode : graph.getNodes()) {
      final Object primaryDiffNode = superNode.getPrimaryDiffNode();

      if (singleObjects.contains(primaryDiffNode)) {
        superObjects.add(superNode);

        continue;
      }

      final Object secondaryDiffNode = superNode.getSecondaryDiffNode();

      if (singleObjects.contains(secondaryDiffNode)) {
        superObjects.add(superNode);
      }
    }

    for (final SuperDiffEdge superEdge : graph.getEdges()) {
      final SingleDiffEdge primaryDiffEdge = superEdge.getPrimaryDiffEdge();

      if (singleObjects.contains(primaryDiffEdge)) {
        superObjects.add(superEdge);

        continue;
      }

      final SingleDiffEdge secondaryDiffEdge = superEdge.getSecondaryDiffEdge();

      if (singleObjects.contains(secondaryDiffEdge)) {
        superObjects.add(superEdge);
      }
    }

    final List<Object> sortedList = SearchResultSorter.getSortedList(superObjects, ESide.PRIMARY);
    setObjectResult(sortedList);
  }
}
