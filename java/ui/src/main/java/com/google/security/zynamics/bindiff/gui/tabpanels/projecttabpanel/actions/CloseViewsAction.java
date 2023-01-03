// Copyright 2011-2023 Google LLC
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
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;

public class CloseViewsAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;

  private final AbstractTable table;

  public CloseViewsAction(final AbstractTable table, final int hitRowIndex) {
    this.table = checkNotNull(table);
    controller = table.getController();
  }

  public CloseViewsAction(final WorkspaceTabPanelFunctions controller) {
    this.controller = checkNotNull(controller);
    this.table = null;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    if (table == null) {
      final Diff diff = controller.getSelectedDiff();
      final Set<Diff> diffSet = new HashSet<>();
      if (diff != null) {
        diffSet.add(diff);
      }
      controller.closeViews(controller.getOpenViews(diffSet));
    } else {
      // Note: Must be a Set<...> because some table could have many rows for the same view.
      // That way can be ensured that such views are closed only once.
      final Set<ViewTabPanel> viewsToClose = new HashSet<>();

      Diff diff = table.getDiff();
      final boolean hasRowDiffs = diff == null;

      for (int index : table.getSelectedRows()) {
        if (hasRowDiffs) {
          diff = AbstractTable.getRowDiff(table, index);
        }

        final Pair<IAddress, IAddress> functionAddrs =
            AbstractTable.getViewAddressPair(table, index);

        if (functionAddrs == null) {
          continue;
        }

        final TabPanelManager tabPanelManager =
            controller.getMainWindow().getController().getTabPanelManager();
        final ViewTabPanel viewPanel =
            tabPanelManager.getTabPanel(functionAddrs.first(), functionAddrs.second(), diff);

        if (viewPanel != null) {
          viewsToClose.add(viewPanel);
        }
      }
      controller.closeViews(viewsToClose);
    }
  }
}
