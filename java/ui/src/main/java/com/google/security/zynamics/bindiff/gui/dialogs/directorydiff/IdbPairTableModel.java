// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.JCheckBoxComparator;
import com.google.security.zynamics.zylib.general.comparators.LexicalComparator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.table.AbstractTableModel;

public class IdbPairTableModel extends AbstractTableModel {
  public static final int SELECTION = 0;
  public static final int IDB_NAME = 1;
  public static final int IDB_LOCATION = 2;
  public static final int WORKSPACE_DESTINATION_DIR = 3;

  private static final String[] COLUMNS = {
    "", "IDB Name", "IDB Location", "Workspace-relative Destination (Editable)"
  };

  private final List<Pair<Integer, Comparator<?>>> sorters = new ArrayList<>();

  private List<DiffPairTableData> tableData = new ArrayList<>();

  public IdbPairTableModel() {
    super();

    initSorters();
  }

  private void initSorters() {
    sorters.add(new Pair<>(SELECTION, new JCheckBoxComparator()));
    sorters.add(new Pair<>(IDB_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(IDB_LOCATION, new LexicalComparator()));
    sorters.add(new Pair<>(WORKSPACE_DESTINATION_DIR, new LexicalComparator()));
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  @Override
  public String getColumnName(final int index) {
    return COLUMNS[index];
  }

  @Override
  public int getRowCount() {
    return tableData.size();
  }

  public List<Pair<Integer, Comparator<?>>> getSorters() {
    return sorters;
  }

  public List<DiffPairTableData> getTableData() {
    return tableData;
  }

  @Override
  public Object getValueAt(final int row, final int column) {
    final DiffPairTableData data = tableData.get(row);

    switch (column) {
      case SELECTION:
        return data.getSelectionCheckBox();
      case IDB_NAME:
        return data.getIDBName();
      case IDB_LOCATION:
        return "." + data.getIDBLocation();
      case WORKSPACE_DESTINATION_DIR:
        return data.getDestinationDirectory();
      default: // fall out
    }

    return null;
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return columnIndex == SELECTION || columnIndex == WORKSPACE_DESTINATION_DIR;
  }

  public void setTableData(final List<DiffPairTableData> tableData) {
    this.tableData = tableData;

    fireTableDataChanged();
  }

  @Override
  public void setValueAt(final Object value, final int row, final int column) {
    if (column == SELECTION) {
      final boolean selected = ((JCheckBox) value).isSelected();

      tableData.get(row).getSelectionCheckBox().setSelected(selected);
    } else if (column == WORKSPACE_DESTINATION_DIR) {
      tableData.get(row).setDestinationDirectory((String) value);
    }
  }
}
