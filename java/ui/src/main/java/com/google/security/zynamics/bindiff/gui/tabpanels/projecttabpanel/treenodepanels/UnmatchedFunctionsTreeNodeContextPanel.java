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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import static com.google.common.base.Preconditions.checkNotNull;

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
    checkNotNull(diff);
    checkNotNull(controller);
    checkNotNull(side);

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
