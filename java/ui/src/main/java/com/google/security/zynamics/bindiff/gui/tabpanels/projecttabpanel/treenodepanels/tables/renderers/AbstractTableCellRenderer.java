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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.userview.ViewManager;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public abstract class AbstractTableCellRenderer extends DefaultTableCellRenderer {
  public static Color NON_ACCESSIBLE_COLOR = Colors.GRAY250;
  public static String NON_ACCESSIBLE_TEXT = "";

  protected static Font NORMAL_FONT = GuiHelper.getDefaultFont();
  protected static Font BOLD_FONT = GuiHelper.getDefaultFont().deriveFont(Font.BOLD);

  protected boolean isBoldFont(final JTable table, final int row) {
    if (!(table instanceof AbstractTable)) {
      return false;
    }

    final AbstractTable absTable = (AbstractTable) table;
    final Diff diff = absTable.getTableModel().getDiff();

    if (diff == null) {
      final Diff rowDiff = AbstractTable.getRowDiff(absTable, row);

      return rowDiff != null && rowDiff.getViewManager().getFlowGraphViewsData().size() > 0;
    }

    final ViewManager viewManager = diff.getViewManager();

    final Pair<IAddress, IAddress> viewAddrPair =
        AbstractTable.getViewAddressPair((AbstractTable) table, row);
    return viewManager.containsView(viewAddrPair.first(), viewAddrPair.second());
  }

  public void buildAndSetToolTip(final JTable table, final int row) {
    if (!(table instanceof AbstractTable)) {
      // Do nothing if not called on BinDiff's own tables
      return;
    }

    final AbstractTable absTable = (AbstractTable) table;
    final Diff diff = absTable.getDiff();

    setToolTipText(absTable.getToolTipForRow(diff, row));
  }

  @Override
  public abstract Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean selected,
      final boolean focused,
      final int row,
      final int column);
}
