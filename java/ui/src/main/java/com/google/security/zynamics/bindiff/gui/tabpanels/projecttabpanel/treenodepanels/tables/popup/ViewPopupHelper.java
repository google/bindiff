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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup;

import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;

public class ViewPopupHelper {
  public static boolean isEnabled(
      final AbstractTable table, final int hitRowIndex, final boolean isOpen) {
    final WorkspaceTabPanelFunctions controller = table.getController();
    final TabPanelManager tabPanelManager =
        controller.getMainWindow().getController().getTabPanelManager();

    Diff diff = table.getDiff();

    final boolean hasRowDiffs = diff == null;
    if (table.getSelectionModel().isSelectedIndex(hitRowIndex)) {
      for (final int selectedRowIndex : table.getSelectedRows()) {
        if (hasRowDiffs) {
          diff = AbstractTable.getRowDiff(table, selectedRowIndex);
          if (!diff.isLoaded()) {
            if (isOpen) {
              return true;
            }

            continue;
          }
        }

        final Pair<IAddress, IAddress> viewAddrPair =
            AbstractTable.getViewAddressPair(table, selectedRowIndex);
        final ViewTabPanel panel =
            tabPanelManager.getTabPanel(viewAddrPair.first(), viewAddrPair.second(), diff);

        if (isOpen && panel == null) {
          return true;
        }

        if (!isOpen && panel != null) {
          return true;
        }
      }

      return false;
    }

    final Pair<IAddress, IAddress> viewAddrPair =
        AbstractTable.getViewAddressPair(table, hitRowIndex);
    final ViewTabPanel panel =
        tabPanelManager.getTabPanel(viewAddrPair.first(), viewAddrPair.second(), diff);

    return isOpen == (panel == null);
  }
}
