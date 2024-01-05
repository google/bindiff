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

package com.google.security.zynamics.bindiff.graph.layout.commands;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.listeners.GraphsIntermediateListeners;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.settings.GraphProximityBrowsingSettings;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.ProximityRangeCalculator;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.HashSet;
import java.util.Set;
import y.base.Node;

public class ProximityBrowserUnhideNode implements ICommand {
  private final BinDiffGraph<ZyGraphNode<?>, ?> graph;
  private final ZyGraphNode<?> nodeToShow;

  public ProximityBrowserUnhideNode(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph, final ZyGraphNode<?> nodeToShow) {
    this.graph = checkNotNull(graph);
    this.nodeToShow = checkNotNull(nodeToShow);
  }

  private static void updateVisibility(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph, final Set<ZyGraphNode<?>> allNodesToShow) {
    for (final ZyGraphNode<? extends IViewNode<?>> node : allNodesToShow) {
      if (node.isVisible()) {
        continue;
      }

      if (node instanceof SingleDiffNode) {
        final Node combinedYNode = ((SingleDiffNode) node).getCombinedDiffNode().getNode();
        final Node superYNode = ((SingleDiffNode) node).getSuperDiffNode().getNode();

        if (!graph.getCombinedGraph().getNode(combinedYNode).getRawNode().isVisible()) {
          graph.getCombinedGraph().getNode(combinedYNode).getRawNode().setVisible(true);
        }

        if (!graph.getSuperGraph().getNode(superYNode).getRawNode().isVisible()) {
          graph.getSuperGraph().getNode(superYNode).getRawNode().setVisible(true);
        }
      } else if (node instanceof CombinedDiffNode) {
        final Node combinedYNode = ((CombinedDiffNode) node).getNode();
        final Node superYNode = ((CombinedDiffNode) node).getSuperDiffNode().getNode();

        if (!graph.getCombinedGraph().getNode(combinedYNode).getRawNode().isVisible()) {
          graph.getCombinedGraph().getNode(combinedYNode).getRawNode().setVisible(true);
        }
        if (!graph.getSuperGraph().getNode(superYNode).getRawNode().isVisible()) {
          graph.getSuperGraph().getNode(superYNode).getRawNode().setVisible(true);
        }
      }
    }
  }

  public static void executeStatic(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph, final ZyGraphNode<?> nodeToShow)
      throws GraphLayoutException {
    final Set<ZyGraphNode<?>> nodesToShow = new HashSet<>();
    nodesToShow.add(nodeToShow);

    final GraphProximityBrowsingSettings settings = graph.getSettings().getProximitySettings();
    final Set<ZyGraphNode<?>> allNodesToShow =
        ProximityRangeCalculator.getNeighbors(
            graph,
            nodesToShow,
            settings.getProximityBrowsingChildren(),
            settings.getProximityBrowsingParents());

    // TODO: Show node visibility warning dialog;
    updateVisibility(graph, allNodesToShow);
    ProximityNodeClickedUpdater.updateProximityNodes(graph);

    GraphViewUpdater.updateViews(graph);
    GraphsIntermediateListeners.notifyIntermediateVisibilityListeners(graph);

    if (LayoutCommandHelper.isAutoLayout(graph)) {
      GraphLayoutUpdater.executeStatic(graph, true);
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(graph, nodeToShow);
  }
}
