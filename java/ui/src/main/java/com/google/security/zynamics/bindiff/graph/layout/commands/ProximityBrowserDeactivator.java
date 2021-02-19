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

package com.google.security.zynamics.bindiff.graph.layout.commands;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.filter.GraphEdgeFilter;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeFilter;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeFilter.Criterion;
import com.google.security.zynamics.bindiff.graph.listeners.GraphsIntermediateListeners;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.JOptionPane;

public class ProximityBrowserDeactivator implements ICommand {
  private final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph;

  public ProximityBrowserDeactivator(
      final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph) {
    checkNotNull(graph);

    this.graph = graph;
  }

  private static boolean hasHiddenNodes(final BinDiffGraph<?, ?> graph) {
    return GraphNodeFilter.filterNodesCountOnly(graph, Criterion.INVISIBLE) > 0;
  }

  private static boolean showAllNodesVisibilityWarningDialog(final BinDiffGraph<?, ?> graph) {
    final int invisibleEdges = GraphEdgeFilter.filterInvisibleEdges(graph).size();
    final int invisibleNodes = GraphNodeFilter.filterNodesCountOnly(graph, Criterion.INVISIBLE);

    final int threshold = graph.getSettings().getLayoutSettings().getVisibilityWarningThreshold();

    if (invisibleEdges >= threshold) {
      final int answer =
          CMessageBox.showYesNoQuestion(
              BinDiffGraph.getParentWindow(graph),
              String.format(
                  "The selected operation makes %d more nodes with %d edges visible. Do you want "
                      + "to continue?",
                  invisibleNodes, invisibleEdges));

      return answer == JOptionPane.YES_OPTION;
    }

    return true;
  }

  private static void unhideAll(
      final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph)
      throws GraphLayoutException {
    ProximityBrowserUpdater.deleteAllProximityNodes(graph.getPrimaryGraph());
    ProximityBrowserUpdater.deleteAllProximityNodes(graph.getSecondaryGraph());

    for (final SuperDiffNode superNode : graph.getSuperGraph().getNodes()) {
      superNode.getRawNode().setVisible(true);
      superNode.getCombinedRawNode().setVisible(true);
    }

    ProximityBrowserUpdater.adoptSuperGraphVisibility(graph.getSuperGraph());

    GraphViewUpdater.updateViews(graph);
  }

  public static void executeStatic(
      final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph)
      throws GraphLayoutException {
    if (!hasHiddenNodes(graph)) {
      return;
    }

    if (showAllNodesVisibilityWarningDialog(graph)) {
      graph.getCombinedGraph().getProximityBrowser().deleteProximityBrowsingNodes();
      graph.getSuperGraph().getProximityBrowser().deleteProximityBrowsingNodes();

      unhideAll(graph);
      GraphsIntermediateListeners.notifyIntermediateVisibilityListeners(graph);

      if (graph.getSettings().getLayoutSettings().getAutomaticLayouting()) {
        GraphLayoutUpdater.executeStatic(graph, true);
      }

      GraphViewUpdater.updateViews(graph);
    } else {
      graph.getSettings().getProximitySettings().setProximityBrowsing(true);
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(graph);
  }
}
