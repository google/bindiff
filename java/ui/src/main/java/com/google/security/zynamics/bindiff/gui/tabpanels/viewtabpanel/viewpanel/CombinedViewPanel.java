package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.viewpanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.CombinedGraphPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.GraphPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import java.awt.BorderLayout;
import javax.swing.JPanel;

public class CombinedViewPanel extends JPanel {
  private final ViewTabPanelFunctions controller;

  private final GraphPanel combinedGraphPanel;

  public CombinedViewPanel(
      final Diff diff, final ViewTabPanelFunctions controller, final ViewData view) {
    super(new BorderLayout());
    Preconditions.checkNotNull(diff);
    Preconditions.checkNotNull(controller);
    Preconditions.checkNotNull(view);

    this.controller = controller;

    combinedGraphPanel = new CombinedGraphPanel(controller, diff, view, EGraph.COMBINED_GRAPH);

    add(combinedGraphPanel, BorderLayout.CENTER);
  }

  public void dispose() {
    combinedGraphPanel.dispose();
  }

  public ViewTabPanelFunctions getController() {
    return controller;
  }

  public GraphPanel getGraphPanel() {
    return combinedGraphPanel;
  }

  public void resetDefaultPerspective() {
    combinedGraphPanel.resetDefaultPerspective();
  }

  public void toggleGraphsPerspective() {
    combinedGraphPanel.toggleGraphsPerspective();
  }
}
