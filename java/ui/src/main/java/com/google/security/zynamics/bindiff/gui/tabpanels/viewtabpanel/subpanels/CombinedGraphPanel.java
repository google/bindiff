package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels;

import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;

public class CombinedGraphPanel extends GraphPanel {
  public CombinedGraphPanel(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final EGraph graphType) {
    super(controller, diff, view, view.getGraphs().getCombinedGraph(), graphType);
  }

  @Override
  public CombinedGraph getGraph() {
    return (CombinedGraph) super.getGraph();
  }
}
