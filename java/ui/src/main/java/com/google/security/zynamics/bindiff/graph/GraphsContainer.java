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

package com.google.security.zynamics.bindiff.graph;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.project.diff.Diff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GraphsContainer extends AbstractGraphsContainer {
  private final List<BinDiffGraph<?, ?>> graphs;

  private Diff diff;

  // Use this Ctor for views belonging to a whole diff object only. (Diff object is available.)
  public GraphsContainer(
      final Diff diff,
      final SuperGraph superGraph,
      final CombinedGraph combinedGraph,
      final SingleGraph primaryGraph,
      final SingleGraph secondaryGraph) {
    Preconditions.checkNotNull(superGraph);
    Preconditions.checkNotNull(combinedGraph);
    Preconditions.checkNotNull(primaryGraph);
    Preconditions.checkNotNull(secondaryGraph);

    this.diff = diff;

    graphs = new ArrayList<>(4);
    graphs.add(EGraph.PRIMARY_GRAPH.ordinal(), primaryGraph);
    graphs.add(EGraph.SECONDARY_GRAPH.ordinal(), secondaryGraph);
    graphs.add(EGraph.COMBINED_GRAPH.ordinal(), combinedGraph);
    graphs.add(EGraph.SUPER_GRAPH.ordinal(), superGraph);

    for (final BinDiffGraph<?, ?> graph : graphs) {
      graph.setGraphs(this);
    }

    combinedGraph.getSelectionHistory().registerMatchListener();
    primaryGraph.getSelectionHistory().registerMatchListener();
    secondaryGraph.getSelectionHistory().registerMatchListener();
  }

  @Override
  public void dispose() {
    for (final BinDiffGraph<?, ?> graph : graphs) {
      if (graph == null) {
        continue;
      }
      graph.dispose();
    }
    graphs.clear();
  }

  @Override
  public CombinedGraph getCombinedGraph() {
    return (CombinedGraph) graphs.get(EGraph.COMBINED_GRAPH.ordinal());
  }

  public Diff getDiff() {
    return diff;
  }

  @Override
  public BinDiffGraph<?, ?> getFocusedGraph() {
    switch (getSettings().getDiffViewMode()) {
      case NORMAL_VIEW:
        {
          if (getSettings().isSync()) {
            return getSuperGraph();
          }
          if (getSettings().getFocus() == ESide.PRIMARY) {
            return getPrimaryGraph();
          }
          return getSecondaryGraph();
        }

      case COMBINED_VIEW:
        return getCombinedGraph();

      case TEXT_VIEW:
        return null;
    }

    throw new IllegalStateException("Illegal view mode.");
  }

  @Override
  public SingleGraph getPrimaryGraph() {
    return (SingleGraph) graphs.get(EGraph.PRIMARY_GRAPH.ordinal());
  }

  @Override
  public SingleGraph getSecondaryGraph() {
    return (SingleGraph) graphs.get(EGraph.SECONDARY_GRAPH.ordinal());
  }

  @Override
  public GraphSettings getSettings() {
    return ((SuperGraph) graphs.get(EGraph.SUPER_GRAPH.ordinal())).getSettings();
  }

  @Override
  public SuperGraph getSuperGraph() {
    return (SuperGraph) graphs.get(EGraph.SUPER_GRAPH.ordinal());
  }

  @Override
  public Iterator<BinDiffGraph<?, ?>> iterator() {
    return graphs.iterator();
  }

  public void setDiff(final Diff diff) {
    this.diff = diff;
  }

  @Override
  public void updateViews() {
    for (final BinDiffGraph<?, ?> graph : graphs) {
      if (graph != null) {
        graph.updateViews();
      }
    }
  }
}
