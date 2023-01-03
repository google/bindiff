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

package com.google.security.zynamics.bindiff.gui.dialogs.searchresultsdialog;

import com.google.security.zynamics.bindiff.graph.searchers.SearchResult;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class SearchResultsCellRenderer extends JLabel implements TableCellRenderer {
  private static final Font INSTRUCTION_FONT = GuiHelper.getMonospacedFont();

  private Border unselectedBorder = null;
  private Border selectedBorder = null;

  public SearchResultsCellRenderer() {
    setOpaque(true); // MUST do this for background to show up.
  }

  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    final SearchResult searchResult = (SearchResult) value;

    setFont(INSTRUCTION_FONT);

    setText(searchResult.getText());

    if (isSelected) {
      setBackground(table.getSelectionBackground());

      if (selectedBorder == null) {
        selectedBorder =
            BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
      }

      setBorder(selectedBorder);
    } else {
      final Color backgroundColor = searchResult.getObjectMarkerColor();

      setBackground(backgroundColor);

      unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, backgroundColor);

      setBorder(unselectedBorder);
    }

    return this;
  }
}
