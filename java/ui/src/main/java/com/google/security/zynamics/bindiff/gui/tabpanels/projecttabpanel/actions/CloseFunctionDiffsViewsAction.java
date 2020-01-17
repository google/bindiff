// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;

public class CloseFunctionDiffsViewsAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;

  private final FunctionDiffViewsTable table;

  public CloseFunctionDiffsViewsAction(final FunctionDiffViewsTable table) {
    this.table = Preconditions.checkNotNull(table);
    this.controller = table.getController();
  }

  public CloseFunctionDiffsViewsAction(final WorkspaceTabPanelFunctions controller) {
    this.controller = Preconditions.checkNotNull(controller);
    this.table = null;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final TabPanelManager tabPanelManager =
        controller.getMainWindow().getController().getTabPanelManager();
    final Set<ViewTabPanel> panelsToClose = new HashSet<>();

    if (table == null) {
      panelsToClose.addAll(tabPanelManager.getViewTabPanels(true));
    } else {
      for (int row = 0; row < table.getRowCount(); ++row) {
        final Diff diff = AbstractTable.getRowDiff(table, row);
        for (final ViewTabPanel panel : tabPanelManager.getViewTabPanels(true)) {
          if (panel.getDiff() == diff) {
            panelsToClose.add(panel);
            break;
          }
        }
      }
    }

    controller.closeViews(panelsToClose);
  }
}
