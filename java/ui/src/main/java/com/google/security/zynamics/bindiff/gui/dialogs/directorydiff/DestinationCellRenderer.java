package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import com.google.common.base.Preconditions;
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
    Preconditions.checkArgument(new File(workspacePath).exists(), "Workspace path must exist");

    this.workspacePath = Preconditions.checkNotNull(workspacePath);
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

    if (column == IdbPairTableModel.DIFF_DESTINATION_DIR) {
      component.setBackground(Color.WHITE);

      final File file = new File(path);

      if (file.exists()) {
        component.setBackground(Colors.TABLE_CELL_PRIMARY_UNMATCHED_BACKGROUND);
      }
    }

    return component;
  }
}
