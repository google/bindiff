// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.gui.zygraph.proximity;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.SelectedVisibleFilter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

/**
 * Updates the visible nodes of a graph when the selection changed and proximity browsing mode is
 * active.
 * 
 * @param <NodeType> Type of the nodes in the graph.
 */
public abstract class ProximityUpdater<NodeType extends ZyGraphNode<?>> implements
    IZyGraphSelectionListener {
  /**
   * Keeps track of the previously selected nodes.
   */
  private Collection<NodeType> m_lastSelectedNodes;

  /**
   * The graph to be updated.
   */
  private final AbstractZyGraph<NodeType, ?> m_graph;

  /**
   * Creates a new proximity browsing updater object.
   * 
   * @param graph The graph to be updated.
   */
  protected ProximityUpdater(final AbstractZyGraph<NodeType, ?> graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");

    m_graph = graph;

    m_lastSelectedNodes = m_graph.getSelectedNodes();
  }

  protected abstract void showNodes(Collection<NodeType> selectedNodes,
      Collection<NodeType> unselectedNodes);

  @Override
  public void selectionChanged() {
    if (!m_graph.getSettings().getProximitySettings().getProximityBrowsing()
        || m_graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
      return;
    }

    final Collection<NodeType> selectedNodes =
        SelectedVisibleFilter.filter(m_graph.getSelectedNodes());

    if (selectedNodes.equals(m_lastSelectedNodes)) {
      // this avoids a proximity browsing update when node selection has not changed
      // occurs if selected nodes a dragged by the mouse in order to move them around
      return;
    }

    m_lastSelectedNodes = selectedNodes;

    // Without this check all nodes are hidden when all nodes are unselected.
    if (!selectedNodes.isEmpty()) {
      final List<NodeType> allNodes = GraphHelpers.getNodes(m_graph);
      allNodes.removeAll(selectedNodes);
      showNodes(selectedNodes, allNodes);
    }
  }
}
