// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import javax.swing.AbstractAction;

public class CloseAllViewsAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;
  private final ViewTabPanel dontClosePanel;

  public CloseAllViewsAction(final WorkspaceTabPanelFunctions controller) {
    this(controller, null);
  }

  public CloseAllViewsAction(
      final WorkspaceTabPanelFunctions controller, final ViewTabPanel dontClosePanel) {
    this.controller = checkNotNull(controller);
    this.dontClosePanel = dontClosePanel;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final LinkedHashSet<ViewTabPanel> tabPanelsToClose = new LinkedHashSet<>();

    final TabPanelManager tabPanelManager =
        controller.getMainWindow().getController().getTabPanelManager();
    for (final ViewTabPanel tabPanel : tabPanelManager.getViewTabPanels()) {
      if (dontClosePanel != null && dontClosePanel == tabPanel) {
        continue;
      }
      tabPanelsToClose.add(tabPanel);
    }

    controller.closeViews(tabPanelsToClose);
  }
}
