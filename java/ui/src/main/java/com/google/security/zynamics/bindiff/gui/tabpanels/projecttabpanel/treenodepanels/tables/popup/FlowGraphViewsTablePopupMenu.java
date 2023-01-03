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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CopySelectionToClipboardAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CopyValueToClipboardAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.OpenMultipeFlowGraphsViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.ShowInCallGraphAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class FlowGraphViewsTablePopupMenu extends JPopupMenu {
  public FlowGraphViewsTablePopupMenu(
      final AbstractTable table, final int hitRowIndex, final int hitColumnIndex) {
    add(
        GuiUtils.buildMenuItem(
            "Open Flow Graph",
            new OpenMultipeFlowGraphsViewsAction(table),
            ViewPopupHelper.isEnabled(table, hitRowIndex, true)));
    add(
        GuiUtils.buildMenuItem(
            "Close Flow Graph",
            new CloseViewsAction(table, hitRowIndex),
            ViewPopupHelper.isEnabled(table, hitRowIndex, false)));
    add(new JSeparator());
    add(GuiUtils.buildMenuItem("Show in Call Graph", new ShowInCallGraphAction(table)));
    add(new Separator());
    add(
        GuiUtils.buildMenuItem(
            "Copy Selection", new CopySelectionToClipboardAction(table), table.hasSelection()));
    add(
        GuiUtils.buildMenuItem(
            "Copy Cell Value", new CopyValueToClipboardAction(table, hitRowIndex, hitColumnIndex)));
  }
}
