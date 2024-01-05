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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.AbstractTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class MatchStateCellRenderer extends AbstractTableCellRenderer {
  private final Color textColor;

  private final Color matchedColor;
  private final Color primaryUnmatchedColor;
  private final Color secondaryUnmatchedColor;

  public MatchStateCellRenderer(
      final Color textColor,
      final Color matchedColor,
      final Color primaryUnmatchedColor,
      final Color secondaryUnmatchedColor) {
    this.textColor = checkNotNull(textColor);
    this.matchedColor = checkNotNull(matchedColor);
    this.primaryUnmatchedColor = checkNotNull(primaryUnmatchedColor);
    this.secondaryUnmatchedColor = checkNotNull(secondaryUnmatchedColor);
  }

  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean selected,
      final boolean focused,
      final int row,
      final int column) {
    checkArgument(value instanceof EMatchState, "Value must be an EMatchState");

    buildAndSetToolTip(table, row);

    final EMatchState matchState = (EMatchState) value;

    final Color backgroundColor;
    if (matchState == EMatchState.PRIMARY_UNMATCHED) {
      backgroundColor = primaryUnmatchedColor;
    } else if (matchState == EMatchState.SECONDRAY_UNMATCHED) {
      backgroundColor = secondaryUnmatchedColor;
    } else {
      backgroundColor = matchedColor;
    }

    setIcon(
        new BackgroundIcon(
            "",
            SwingConstants.LEFT,
            textColor,
            backgroundColor,
            table.getSelectionBackground(),
            selected,
            0 - 1,
            0,
            table.getColumnModel().getColumn(column).getWidth() - 1,
            table.getRowHeight() - 1));

    return this;
  }
}
