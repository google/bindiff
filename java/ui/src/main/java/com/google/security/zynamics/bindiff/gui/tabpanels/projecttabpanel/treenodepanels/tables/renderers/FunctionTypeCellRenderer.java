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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.BackgroundIcon;
import com.google.security.zynamics.bindiff.resources.Colors;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class FunctionTypeCellRenderer extends AbstractTableCellRenderer {
  public static Color calcColor(final EFunctionType functionType) {
    switch (functionType) {
      case NORMAL:
        return Colors.GRAY250;
      case LIBRARY:
        return Colors.FUNCTION_TYPE_LIBRARY;
      case THUNK:
        return Colors.FUNCTION_TYPE_THUNK;
      case IMPORTED:
        return Colors.FUNCTION_TYPE_IMPORTED;
      case UNKNOWN:
        return Colors.GRAY250;
      default:
        return Colors.GRAY160;
    }
  }

  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean selected,
      final boolean focused,
      final int row,
      final int column) {
    buildAndSetToolTip(table, row);

    setFont(!isBoldFont(table, row) ? NORMAL_FONT : BOLD_FONT);

    if (value instanceof EFunctionType) {
      final boolean accessable = !value.toString().equals(EFunctionType.UNKNOWN.toString());

      setIcon(
          new BackgroundIcon(
              accessable ? value.toString() : NON_ACCESSIBLE_TEXT,
              SwingConstants.CENTER,
              Colors.GRAY32,
              accessable ? calcColor((EFunctionType) value) : NON_ACCESSIBLE_COLOR,
              table.getSelectionBackground(),
              selected,
              0 - 1,
              0,
              table.getColumnModel().getColumn(column).getWidth() - 1,
              table.getRowHeight() - 1));
    }

    return this;
  }
}
