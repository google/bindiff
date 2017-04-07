package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels;

import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.userview.ViewData;

public class SuperGraphPanel extends GraphPanel {
  public SuperGraphPanel(
      final ViewTabPanelFunctions controller, final ViewData view, final EGraph graphType) {
    super(
        controller, view.getGraphs().getDiff(), view, view.getGraphs().getSuperGraph(), graphType);
  }

  @Override
  public SuperGraph getGraph() {
    return (SuperGraph) super.getGraph();
  }
}
