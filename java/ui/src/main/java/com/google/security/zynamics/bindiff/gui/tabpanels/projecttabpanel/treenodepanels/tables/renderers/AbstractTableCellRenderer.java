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

  protected static Font NORMAL_FONT = GuiHelper.DEFAULT_FONT;
  protected static Font BOLD_FONT =
      new Font(NORMAL_FONT.getName(), Font.BOLD, NORMAL_FONT.getSize());

  protected boolean isBoldFont(final JTable table, final int row) {
    if (!(table instanceof AbstractTable)) {
      return false;
    }

    final AbstractTable absTable = (AbstractTable) table;
    final Diff diff = absTable.getTableModel().getDiff();

    if (diff == null) {
      final Diff rowDiff = AbstractTable.getRowDiff(absTable, row);

      return rowDiff != null && rowDiff.getViewManager().getFlowgraphViewsData().size() > 0;
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
