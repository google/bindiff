package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.tables.CTableSorter;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

public class IdbPairTable extends JTable // AbstractTable
{
  private final CTableSorter tableSorter;

  private final IdbPairTableModel model;

  public IdbPairTable(final File workspaceDir, final IdbPairTableModel model) {
    Preconditions.checkNotNull(workspaceDir);
    this.model = Preconditions.checkNotNull(model);

    tableSorter = new CTableSorter(model);

    setModel(tableSorter);

    tableSorter.setTableHeader(getTableHeader());

    for (final Pair<Integer, Comparator<?>> sorter : model.getSorters()) {
      tableSorter.setColumnComparator(sorter.first(), sorter.second());
    }

    getModel().getTableHeader().setToolTipText("Press CTRL to add secondary sort.");

    tableSorter.setTableHeader(getTableHeader());

    setRowSelectionAllowed(false);
    setColumnSelectionAllowed(false);
    setCellSelectionEnabled(false);

    final TableColumn selectionBox = getColumnModel().getColumn(IdbPairTableModel.SELECTION);
    final TableColumn idbName = getColumnModel().getColumn(IdbPairTableModel.IDB_NAME);
    final TableColumn idbLocation = getColumnModel().getColumn(IdbPairTableModel.IDB_LOCATION);
    final TableColumn diffDestination =
        getColumnModel().getColumn(IdbPairTableModel.DIFF_DESTINATION_DIR);

    selectionBox.setMaxWidth(30);
    selectionBox.setWidth(30);
    selectionBox.setMinWidth(30);

    idbName.setMinWidth(150);
    idbName.setWidth(150);

    diffDestination.setMinWidth(375);
    diffDestination.setWidth(375);

    final CheckboxCellRenderer selectionBoxRenderer = new CheckboxCellRenderer();
    selectionBox.setCellRenderer(selectionBoxRenderer);
    selectionBox.setCellEditor(new CheckboxCellEditor());

    final DefaultCellRenderer defaultRenderer = new DefaultCellRenderer();
    idbName.setCellRenderer(defaultRenderer);
    idbLocation.setCellRenderer(defaultRenderer);

    final DestinationCellRenderer destinationCellRenderer =
        new DestinationCellRenderer(workspaceDir.getPath());
    diffDestination.setCellRenderer(destinationCellRenderer);
  }

  @Override
  public CTableSorter getModel() {
    return tableSorter;
  }

  public IdbPairTableModel getTableModel() {
    return model;
  }

  public void setTableData(final List<DiffPairTableData> tableData) {
    model.setTableData(tableData);
  }
}
