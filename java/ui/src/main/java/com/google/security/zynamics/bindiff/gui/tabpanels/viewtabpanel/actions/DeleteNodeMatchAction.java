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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.helpers.BasicBlockMatchRemover;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;

public class DeleteNodeMatchAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;

  private final BinDiffGraph<?, ?> graph;

  private final ZyGraphNode<?> clickedNode;

  public DeleteNodeMatchAction(final ViewTabPanelFunctions controller) {
    this(controller, null, null);
  }

  public DeleteNodeMatchAction(
      final ViewTabPanelFunctions controller,
      final BinDiffGraph<?, ?> graph,
      final ZyGraphNode<?> clickedNode) {
    super("Delete Basic Block Matches");

    this.controller = checkNotNull(controller);
    this.graph = graph;
    this.clickedNode = clickedNode;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final GraphsContainer graphs = controller.getGraphs();
    if (graph == null || clickedNode == null) {
      final EDiffViewMode diffViewMode = controller.getGraphSettings().getDiffViewMode();

      BinDiffGraph<?, ?> graph = null;
      if (diffViewMode == EDiffViewMode.COMBINED_VIEW) {
        graph = graphs.getCombinedGraph();
      } else if (diffViewMode == EDiffViewMode.NORMAL_VIEW) {
        graph = graphs.getPrimaryGraph();
      }

      final List<CombinedDiffNode> affectedNodes =
          BasicBlockMatchRemover.getAffectedCombinedNodes(graph);

      if (affectedNodes != null) {
        controller.removeNodeMatch(affectedNodes);
      }
    } else {
      final List<CombinedDiffNode> affectedNodes =
          BasicBlockMatchRemover.getAffectedCombinedNodes(graph, clickedNode);

      if (affectedNodes != null) {
        controller.removeNodeMatch(affectedNodes);
      }
    }
  }
}
