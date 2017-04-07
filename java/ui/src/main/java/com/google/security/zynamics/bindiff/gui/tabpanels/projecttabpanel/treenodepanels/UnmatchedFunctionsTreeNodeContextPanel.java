package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.subpanels.UnmatchedFunctionViewsPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JSplitPane;

public class UnmatchedFunctionsTreeNodeContextPanel extends AbstractTreeNodeContextPanel {
  private final ESide side;

  private final UnmatchedFunctionViewsPanel primaryUnmatchedFunctionViewPanel;
  private final UnmatchedFunctionViewsPanel secondaryUnmatchedFunctionViewPanel;

  public UnmatchedFunctionsTreeNodeContextPanel(
      final Diff diff, final WorkspaceTabPanelFunctions controller, final ESide side) {
    Preconditions.checkNotNull(diff);
    Preconditions.checkNotNull(controller);
    Preconditions.checkNotNull(side);

    this.side = side;

    primaryUnmatchedFunctionViewPanel =
        new UnmatchedFunctionViewsPanel(diff, controller, ESide.PRIMARY);
    secondaryUnmatchedFunctionViewPanel =
        new UnmatchedFunctionViewsPanel(diff, controller, ESide.SECONDARY);

    init();
  }

  private void init() {

    final JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
    splitPanel.setBorder(null);
    splitPanel.setOneTouchExpandable(true);
    splitPanel.setResizeWeight(1.);
    splitPanel.setDividerLocation(0.2);

    splitPanel.setTopComponent(
        side == ESide.PRIMARY
            ? primaryUnmatchedFunctionViewPanel
            : secondaryUnmatchedFunctionViewPanel);

    splitPanel.setBottomComponent(
        side == ESide.PRIMARY
            ? secondaryUnmatchedFunctionViewPanel
            : primaryUnmatchedFunctionViewPanel);

    add(splitPanel, BorderLayout.CENTER);
  }

  @Override
  public List<AbstractTable> getTables() {
    return side == ESide.PRIMARY
        ? primaryUnmatchedFunctionViewPanel.getTables()
        : secondaryUnmatchedFunctionViewPanel.getTables();
  }
}
