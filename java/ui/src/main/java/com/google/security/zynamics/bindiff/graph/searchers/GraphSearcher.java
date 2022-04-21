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
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.sorters.SearchResultSorter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphSearcher {
  private boolean isRegEx;
  private boolean isCaseSensitive;
  private boolean onlySelected;
  private boolean onlyVisible;

  private boolean changed = true;

  private boolean isAfterLast = false;
  private boolean isBeforeFirst = false;

  private int currentSearchIndex = -1;
  private String lastSearchString;

  private final List<SearchResult> subObjectResults = new ArrayList<>();

  private List<Object> objectResults = new ArrayList<>();

  private List<ZyGraphEdge<?, ?, ?>> filterValidEdgesNodes(
      final List<? extends ZyGraphEdge<?, ?, ?>> egdes) {
    final List<ZyGraphEdge<?, ?, ?>> validEdges = new ArrayList<>();

    for (final ZyGraphEdge<?, ?, ?> edge : egdes) {
      if (edge instanceof SingleDiffEdge
          || edge instanceof CombinedDiffEdge
          || edge instanceof SuperDiffEdge) {
        validEdges.add(edge);
      }
    }

    return validEdges;
  }

  private List<ZyGraphNode<?>> filterValidNodes(final List<? extends ZyGraphNode<?>> nodes) {
    final List<ZyGraphNode<?>> validNodes = new ArrayList<>();

    for (final ZyGraphNode<?> node : nodes) {
      if (node instanceof SingleDiffNode
          || node instanceof CombinedDiffNode
          || node instanceof SuperDiffNode) {
        validNodes.add(node);
      }
    }

    return validNodes;
  }

  private void searchAll(
      final List<ZyGraphNode<?>> nodes,
      final List<ZyGraphEdge<?, ?, ?>> edges,
      final String searchString) {
    for (final ZyGraphNode<?> node : nodes) {
      searchNode(node, searchString);
    }

    for (final ZyGraphEdge<?, ?, ?> edge : edges) {
      searchEdge(edge, searchString);
    }
  }

  private void searchEdge(final ZyGraphEdge<?, ?, ?> edge, final String searchString) {
    subObjectResults.addAll(EdgeSearcher.search(edge, searchString, isRegEx, isCaseSensitive));

    if (subObjectResults.size() != 0) {
      currentSearchIndex = 0;
    }
  }

  private void searchNode(final ZyGraphNode<?> node, final String searchString) {
    subObjectResults.addAll(NodeSearcher.search(node, searchString, isRegEx, isCaseSensitive));

    if (subObjectResults.size() != 0) {
      currentSearchIndex = 0;
    }
  }

  private void searchSelected(
      final List<ZyGraphNode<?>> nodes,
      final List<ZyGraphEdge<?, ?, ?>> edges,
      final String searchString) {
    for (final ZyGraphNode<?> node : nodes) {
      if (node.isSelected()) {
        searchNode(node, searchString);
      }
    }

    for (final ZyGraphEdge<?, ?, ?> edge : edges) {
      if (edge.isSelected()) {
        searchEdge(edge, searchString);
      }
    }
  }

  private void searchVisible(
      final List<ZyGraphNode<?>> nodes,
      final List<ZyGraphEdge<?, ?, ?>> edges,
      final String searchString) {
    for (final ZyGraphNode<?> node : nodes) {
      if (node.isVisible()) {
        searchNode(node, searchString);
      }
    }

    for (final ZyGraphEdge<?, ?, ?> edge : edges) {
      if (edge.isVisible()) {
        searchEdge(edge, searchString);
      }
    }
  }

  protected void setLastSearchString(final String searchString) {
    changed = false;
    lastSearchString = searchString;
  }

  protected void setObjectResult(final List<Object> objects) {
    objectResults = objects;
  }

  protected void sortResult() {
    Collections.sort(subObjectResults, new SearchResultComparator());

    final Set<Object> objects = new HashSet<>();

    for (final SearchResult result : subObjectResults) {
      objects.add(result.getObject());
    }

    objectResults = SearchResultSorter.getSortedList(objects, ESide.PRIMARY);
  }

  public void clearResults() {
    for (final SearchResult result : subObjectResults) {
      GraphSearcherFunctions.removeSubObjectHighlighting(result);
    }

    subObjectResults.clear();
    objectResults.clear();

    currentSearchIndex = 0;

    lastSearchString = "";
    changed = false;
  }

  public Object current() {
    return objectResults.size() == 0 ? null : objectResults.get(currentSearchIndex);
  }

  public boolean getHasChanged(final String searchString) {
    return changed || !searchString.equals(lastSearchString);
  }

  public String getLastSearchString() {
    return lastSearchString;
  }

  public List<Object> getObjectResults() {
    return objectResults;
  }

  public List<SearchResult> getSubObjectResults() {
    return subObjectResults;
  }

  public boolean isAfterLast() {
    return isAfterLast;
  }

  public boolean isBeforeFirst() {
    return isBeforeFirst;
  }

  public boolean isCaseSensitive() {
    return isCaseSensitive;
  }

  public boolean isRegEx() {
    return isRegEx;
  }

  public boolean isSelectedOnly() {
    return onlySelected;
  }

  public boolean isVisibleOnly() {
    return onlyVisible;
  }

  public void next() {
    if (current() == null) {
      return;
    }

    currentSearchIndex++;

    isBeforeFirst = false;
    isAfterLast = false;

    if (currentSearchIndex == objectResults.size()) {
      currentSearchIndex = 0;

      isAfterLast = true;
    }
  }

  public void previous() {
    if (current() == null) {
      return;
    }

    currentSearchIndex--;

    isBeforeFirst = false;
    isAfterLast = false;

    if (currentSearchIndex < 0) {
      currentSearchIndex = objectResults.size() - 1;

      isBeforeFirst = true;
    }
  }

  public void search(
      final List<? extends ZyGraphNode<?>> nodes,
      final List<? extends ZyGraphEdge<?, ?, ?>> edges,
      final String searchString) {
    final List<ZyGraphNode<?>> validNodes = filterValidNodes(nodes);
    final List<ZyGraphEdge<?, ?, ?>> validEdges = filterValidEdgesNodes(edges);

    setLastSearchString(searchString);

    if (onlySelected) {
      searchSelected(validNodes, validEdges, searchString);
    } else if (onlyVisible) {
      searchVisible(validNodes, validEdges, searchString);
    } else {
      searchAll(validNodes, validEdges, searchString);
    }

    sortResult();
  }

  public void setCaseSensitive(final boolean caseSensitive) {
    isCaseSensitive = caseSensitive;

    changed = true;
  }

  public void setOnlySelected(final boolean selected) {
    onlySelected = selected;

    changed = true;
  }

  public void setOnlyVisible(final boolean visible) {
    onlyVisible = visible;

    changed = true;
  }

  public void setRegEx(final boolean regEx) {
    isRegEx = regEx;

    changed = true;
  }
}
