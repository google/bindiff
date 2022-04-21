// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;

public class CloseViewAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;
  private ViewTabPanel viewPanel;

  public CloseViewAction(final ViewTabPanel viewPanel) {
    this.controller = null;
    this.viewPanel = checkNotNull(viewPanel);
  }

  public CloseViewAction(final ViewTabPanelFunctions controller) {
    this.controller = checkNotNull(controller);
    this.viewPanel = null;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    execute();
  }

  public void execute() {
    if (controller != null) {
      final JTabbedPane tabbedPane = controller.getTabPanelManager().getTabbedPane();
      viewPanel =
          tabbedPane.getSelectedIndex() > 0
              ? (ViewTabPanel) tabbedPane.getSelectedComponent()
              : null;
    }
    if (viewPanel != null) {
      final TabPanelManager tabPanelManager = viewPanel.getController().getTabPanelManager();
      final WorkspaceTabPanelFunctions controller =
          tabPanelManager.getWorkspaceTabPanel().getController();

      final LinkedHashSet<ViewTabPanel> tabPanelsToClose = new LinkedHashSet<>();
      tabPanelsToClose.add(viewPanel);
      controller.closeViews(tabPanelsToClose);
    }
  }
}
