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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.searchers.GraphSeacherFunctions;
import com.google.security.zynamics.bindiff.graph.searchers.NodeSearcher;
import com.google.security.zynamics.bindiff.graph.searchers.SearchResult;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeNodeSearcher {
  private final ListenerProvider<ITreeNodeSearcherListener> listeners = new ListenerProvider<>();

  private final ArrayList<SearchResult> subObjectResults = new ArrayList<>();
  private final Set<ZyGraphNode<?>> resultNodes = new HashSet<>();
  private boolean isPrimary;
  private boolean isSecondary;
  private boolean isRegEx;
  private boolean isCaseSensitive;
  private boolean highlightGraphNodes;
  private boolean useTemporaryResult;
  private String searchText;

  public TreeNodeSearcher(
      final boolean regEx,
      final boolean isCaseSensitive,
      final boolean isPrimary,
      final boolean isSecondary,
      final boolean useTemporaryResult,
      final boolean highlightGraphNodes) {
    this.isRegEx = regEx;
    this.isCaseSensitive = isCaseSensitive;
    this.isPrimary = isPrimary;
    this.isSecondary = isSecondary;
    this.useTemporaryResult = useTemporaryResult;
    this.highlightGraphNodes = highlightGraphNodes;
    this.searchText = "";
  }

  private ArrayList<SearchResult> search(final ZyGraphNode<?> graphNode) {
    if (graphNode == null) {
      return new ArrayList<>();
    }

    return NodeSearcher.search(graphNode, searchText, isRegEx, isCaseSensitive);
  }

  public void addListener(final ITreeNodeSearcherListener listener) {
    listeners.addListener(listener);
  }

  public boolean getHighlightGraphNodes() {
    return highlightGraphNodes;
  }

  public String getSearchString() {
    return searchText;
  }

  public boolean getUseTemporaryResults() {
    return useTemporaryResult;
  }

  public boolean isSearchHit(final ZyGraphNode<?> graphNode) {
    return resultNodes.contains(graphNode);
  }

  public void notifyListeners() {
    for (final ITreeNodeSearcherListener listener : listeners) {
      listener.searchStringChanged(this);
    }
  }

  public void removeListener(final ITreeNodeSearcherListener listener) {
    listeners.removeListener(listener);
  }

  public List<? extends ISearchableTreeNode> search(
      final List<? extends ISearchableTreeNode> treeNodes) {
    if ("".equals(searchText) || treeNodes == null) {
      GraphSeacherFunctions.removeHightlighing(subObjectResults);
      subObjectResults.clear();

      return treeNodes;
    }

    final List<ISearchableTreeNode> result = new ArrayList<>();

    for (final ISearchableTreeNode treeNode : treeNodes) {
      final ZyGraphNode<?> priGraphNode = treeNode.getGraphNode(ESide.PRIMARY);
      final ZyGraphNode<?> secGraphNode = treeNode.getGraphNode(ESide.SECONDARY);

      boolean added = false;
      if (isPrimary) {
        final ArrayList<SearchResult> results = search(priGraphNode);

        if (results.size() != 0 && !added) {
          resultNodes.add(treeNode.getGraphNode());
          result.add(treeNode);
          added = true;
        }
      }

      if (isSecondary) {
        final ArrayList<SearchResult> results = search(secGraphNode);

        if (results.size() != 0 && !added) {
          resultNodes.add(treeNode.getGraphNode());
          result.add(treeNode);
          added = true;
        }
      }
    }

    GraphSeacherFunctions.highlightResults(subObjectResults);

    return result;
  }

  public void setSearchSettings(
      final boolean isRegEx,
      final boolean isCaseSensitive,
      final boolean isPrimary,
      final boolean isSecondary,
      final boolean useTemporaryResult,
      final boolean highlightGraphNodes) {
    this.isRegEx = isRegEx;
    this.isCaseSensitive = isCaseSensitive;
    this.isPrimary = isPrimary;
    this.isSecondary = isSecondary;
    this.useTemporaryResult = useTemporaryResult;
    this.highlightGraphNodes = highlightGraphNodes;
  }

  public void setSearchString(final String searchText) {
    this.searchText = searchText;

    notifyListeners();
  }

  public void setUseTemporaryResults(final boolean useTemporaryResults) {
    useTemporaryResult = useTemporaryResults;
  }
}
