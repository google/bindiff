package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.helpers.BasicBlockMatchAdder;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class AddNodeMatchAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;
  private final BinDiffGraph<?, ?> graph;
  private final ZyGraphNode<?> clickedNode;

  public AddNodeMatchAction(final ViewTabPanelFunctions controller) {
    this(controller, null, null);
  }

  public AddNodeMatchAction(
      final ViewTabPanelFunctions controller,
      final BinDiffGraph<?, ?> graph,
      final ZyGraphNode<?> clickedNode) {
    super("Add Basic Block Match");

    this.controller = Preconditions.checkNotNull(controller);
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

      final Pair<CombinedDiffNode, CombinedDiffNode> nodePair =
          BasicBlockMatchAdder.getAffectedCombinedNodes(graph);

      if (nodePair != null) {
        controller.addNodeMatch(nodePair.first(), nodePair.second());
      }
    } else {
      final Pair<CombinedDiffNode, CombinedDiffNode> nodePair =
          BasicBlockMatchAdder.getAffectedCombinedNodes(graph, clickedNode);

      if (nodePair != null) {
        controller.addNodeMatch(nodePair.first(), nodePair.second());
      }
    }
  }
}
