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

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.BackgroundIcon;
import com.google.security.zynamics.bindiff.resources.Colors;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class SimilarityConfidenceCellRenderer extends AbstractTableCellRenderer {

  /**
   * Calculates a match color based on the value. Uses the same color palette as the C++ plugin
   * code.
   */
  public static Color calcColor(final double value) {
    if (value < 0 || value > 1) {
      return Colors.GRAY192;
    }
    final Color[] colorRamp =
        BinDiffConfig.getInstance().getThemeSettings().getSimilarityColorRamp();
    return colorRamp[(int) (value * (colorRamp.length - 1))];
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

    if (value instanceof Double) {
      final double d = (Double) value;

      setIcon(
          new BackgroundIcon(
              d == -1 ? NON_ACCESSIBLE_TEXT : String.format("%.2f", d),
              SwingConstants.CENTER,
              Colors.GRAY32,
              d == -1 ? NON_ACCESSIBLE_COLOR : calcColor((Double) value),
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
