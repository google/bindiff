// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.resources.Colors;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DestinationCellRenderer extends DefaultTableCellRenderer {
  private final String workspacePath;

  public DestinationCellRenderer(final String workspacePath) {
    // TODO(cblichmann): File checks are expensive in a GUI component.
    checkArgument(new File(workspacePath).exists(), "Workspace path must exist");

    this.workspacePath = checkNotNull(workspacePath);
  }

  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    final Component component =
        super.getTableCellRendererComponent(table, value, false, false, row, column);

    final String path = String.format("%s%s%s", workspacePath, File.separator, value);

    if (column == IdbPairTableModel.WORKSPACE_DESTINATION_DIR) {
      component.setBackground(Color.WHITE);

      final File file = new File(path);

      if (file.exists()) {
        component.setBackground(Colors.TABLE_CELL_PRIMARY_UNMATCHED_BACKGROUND);
      }
    }

    return component;
  }
}
