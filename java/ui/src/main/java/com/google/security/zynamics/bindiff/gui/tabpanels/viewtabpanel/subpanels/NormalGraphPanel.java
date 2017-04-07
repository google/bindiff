package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels;

import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;

public class NormalGraphPanel extends GraphPanel {
  public NormalGraphPanel(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final ESide side) {
    super(controller, diff, view, getSingleGraph(view.getGraphs(), side), getGraphType(side));
  }

  private static EGraph getGraphType(final ESide side) {
    return side == ESide.PRIMARY ? EGraph.PRIMARY_GRAPH : EGraph.SECONDARY_GRAPH;
  }

  private static BinDiffGraph<?, ?> getSingleGraph(final GraphsContainer graphs, final ESide side) {
    if (side == ESide.PRIMARY) {
      return graphs.getPrimaryGraph();
    } else if (side == ESide.SECONDARY) {
      return graphs.getSecondaryGraph();
    }

    throw new IllegalArgumentException("Illegal graph type.");
  }
}
