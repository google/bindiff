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

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarIcon;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;

public class PercentageThreeBarCellRenderer extends AbstractTableCellRenderer {
  private final Color leftBarColor;
  private final Color centerBarColor;
  private final Color rightBarColor;
  private final Color emptyBarColor;
  private final Color textColor;

  public PercentageThreeBarCellRenderer(
      final Color leftBarColor,
      final Color centerBarColor,
      final Color rightBarColor,
      final Color emptyBarColor,
      final Color textColor) {
    this.leftBarColor = leftBarColor;
    this.centerBarColor = centerBarColor;
    this.rightBarColor = rightBarColor;
    this.emptyBarColor = emptyBarColor;
    this.textColor = textColor;
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

    if (value instanceof PercentageThreeBarCellData) {
      setIcon(
          new PercentageThreeBarIcon(
              (PercentageThreeBarCellData) value,
              leftBarColor,
              centerBarColor,
              rightBarColor,
              emptyBarColor,
              textColor,
              table.getSelectionBackground(),
              selected,
              0 - 1,
              0,
              table.getColumnModel().getColumn(column).getWidth() - 2,
              table.getRowHeight() - 2));
    }

    return this;
  }
}
