package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.viewpanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.NormalGraphPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.SuperGraphPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.bindiff.resources.Constants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

public class CNormalViewPanel extends JPanel {
  private static final double DEFAULT_DIVIDER_LOCATION = 0.5;

  private final JSplitPane mainSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

  private final NormalGraphPanel primaryPanel;
  private final NormalGraphPanel secondaryPanel;

  private final ViewTabPanelFunctions controller;

  public CNormalViewPanel(
      final Diff diff, final ViewTabPanelFunctions controller, final ViewData viewData) {
    super(new BorderLayout());

    this.controller = Preconditions.checkNotNull(controller);
    Preconditions.checkNotNull(viewData);

    primaryPanel = new NormalGraphPanel(controller, diff, viewData, ESide.PRIMARY);
    secondaryPanel = new NormalGraphPanel(controller, diff, viewData, ESide.SECONDARY);

    initPanel(viewData);

    showSupergraph(viewData);
  }

  private void initPanel(final ViewData viewData) {
    mainSplitter.setBorder(new EmptyBorder(0, 0, 0, 0));

    mainSplitter.setLeftComponent(primaryPanel);
    mainSplitter.setRightComponent(secondaryPanel);

    mainSplitter.setMinimumSize(new Dimension(0, 0));
    mainSplitter.setOneTouchExpandable(true);
    mainSplitter.setDoubleBuffered(true);
    mainSplitter.setContinuousLayout(true);
    mainSplitter.setResizeWeight(DEFAULT_DIVIDER_LOCATION);
    mainSplitter.setDividerLocation(DEFAULT_DIVIDER_LOCATION);
    mainSplitter.setLastDividerLocation(mainSplitter.getDividerLocation());

    add(mainSplitter, BorderLayout.CENTER);

    if (viewData.isFlowgraphView()) {
      if (viewData.getAddress(ESide.PRIMARY) == null) {
        mainSplitter.setDividerLocation(0.d);
      } else if (viewData.getAddress(ESide.SECONDARY) == null) {

        mainSplitter.setDividerLocation(4192);
      }
    }
  }

  private void showSupergraph(final ViewData viewData) {
    if (Constants.SHOW_SUPERGRAPH) {
      final JFrame frame = new JFrame();
      frame.setAlwaysOnTop(true);
      frame.setLayout(new BorderLayout());

      frame.add(new SuperGraphPanel(controller, viewData, EGraph.SUPER_GRAPH));

      frame.pack();
      frame.setVisible(true);
    }
  }

  public void dispose() {
    primaryPanel.dispose();
    secondaryPanel.dispose();
  }

  public ViewTabPanelFunctions getController() {
    return controller;
  }

  public NormalGraphPanel getPrimaryPanel() {
    return primaryPanel;
  }

  public NormalGraphPanel getSecondaryPanel() {
    return secondaryPanel;
  }

  public void resetDefaultPerspective() {
    primaryPanel.resetDefaultPerspective();
    secondaryPanel.resetDefaultPerspective();
    mainSplitter.setResizeWeight(DEFAULT_DIVIDER_LOCATION);
    mainSplitter.setDividerLocation(DEFAULT_DIVIDER_LOCATION);
  }

  public void toggleGraphsPerspective() {
    primaryPanel.toggleGraphsPerspective();
    secondaryPanel.toggleGraphsPerspective();
  }

  public void togglePrimaryPerspective() {
    if (mainSplitter.getRightComponent().getSize().width == 0.) {
      resetDefaultPerspective();
    } else {
      primaryPanel.setMainDividerLocationAndResizeWeight(0.2);
      mainSplitter.setDividerLocation(1.);
    }
  }

  public void toggleSecondaryPerspective() {
    if (mainSplitter.getDividerLocation() != 0) {
      secondaryPanel.setMainDividerLocationAndResizeWeight(0.8);
      mainSplitter.setDividerLocation(0.);
    } else {
      resetDefaultPerspective();
    }
  }
}
