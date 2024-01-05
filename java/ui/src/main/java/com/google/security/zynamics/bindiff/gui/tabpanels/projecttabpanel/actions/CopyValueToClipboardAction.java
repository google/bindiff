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

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.ExtendedMatchedFunctionViewsTableModel;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class CopyValueToClipboardAction extends AbstractAction {
  private final AbstractTable table;
  private final int hitRowIndex;
  private final int hitColumnIndex;

  public CopyValueToClipboardAction(
      final AbstractTable table, final int hitRowIndex, final int hitColumnIndex) {
    this.table = checkNotNull(table);
    this.hitRowIndex = hitRowIndex;
    this.hitColumnIndex = hitColumnIndex;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    String value = "";
    final Object obj = table.getModel().getValueAt(hitRowIndex, hitColumnIndex);
    if (obj == ExtendedMatchedFunctionViewsTableModel.ADDED_ICON) {
      value = "+";
    } else if (obj == ExtendedMatchedFunctionViewsTableModel.REMOVED_ICON) {
      value = "-";
    } else {
      value = table.getModel().getValueAt(hitRowIndex, hitColumnIndex).toString();
    }

    ClipboardHelpers.copyToClipboard(value);
  }
}
