package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.AbstractTableCellRenderer;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class IconCellRenderer extends AbstractTableCellRenderer {
  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean selected,
      final boolean focused,
      final int row,
      final int column) {
    Preconditions.checkArgument(value instanceof Icon, "Value must be an Icon.");

    setHorizontalAlignment(SwingConstants.CENTER);
    setIcon((Icon) value);

    return this;
  }
}
