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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.tables.CTableSorter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public abstract class AbstractTable extends JTable {
  private final AbstractTableModel model;

  private final WorkspaceTabPanelFunctions controller;

  private final CTableSorter tableSorter;

  private final ListenerProvider<IViewsTableListener> listeners = new ListenerProvider<>();

  private final InternalMouseListener mouseListener = new InternalMouseListener();

  private final InternalSelectionListener selectionListener = new InternalSelectionListener();

  public AbstractTable(
      final AbstractTableModel model, final WorkspaceTabPanelFunctions controller) {
    this.model = checkNotNull(model);
    this.controller = checkNotNull(controller);

    tableSorter = new CTableSorter(model);

    setModel(tableSorter);

    tableSorter.setTableHeader(getTableHeader());

    for (final Pair<Integer, Comparator<?>> sorter : model.getSorters()) {
      tableSorter.setColumnComparator(sorter.first(), sorter.second());
    }

    getModel().getTableHeader().setToolTipText("Press CTRL to add secondary sort.");

    addMouseListener(mouseListener);
    getSelectionModel().addListSelectionListener(selectionListener);
  }

  public static Diff getRowDiff(final AbstractTable absTable, final int row) {
    final int index = absTable.getModel().modelIndex(row);

    if (absTable.getTableModel() instanceof FunctionDiffViewsContainerTableModel) {
      final FunctionDiffViewsContainerTableModel tableModel =
          (FunctionDiffViewsContainerTableModel) absTable.getTableModel();
      return tableModel.getDiffAt(index);
    } else if (absTable.getTableModel() instanceof FunctionDiffViewsTableModel) {
      final FunctionDiffViewsTableModel tableModel =
          (FunctionDiffViewsTableModel) absTable.getTableModel();
      return tableModel.getDiffAt(index);
    }

    return absTable.getDiff();
  }

  public static Pair<IAddress, IAddress> getViewAddressPair(
      final AbstractTable table, final int rowIndex) {
    IAddress priAddr = null;
    IAddress secAddr = null;

    if (table instanceof MatchedFunctionViewsTable) {
      priAddr =
          new CAddress(
              table
                  .getValueAt(rowIndex, MatchedFunctionsViewsTableModel.PRIMARY_ADDRESS)
                  .toString(),
              16);
      secAddr =
          new CAddress(
              table
                  .getValueAt(rowIndex, MatchedFunctionsViewsTableModel.SECONDARY_ADDRESS)
                  .toString(),
              16);
    } else if (table instanceof ExtendedMatchedFunctionViewsTable) {

      final String priAddrString =
          table
              .getValueAt(rowIndex, ExtendedMatchedFunctionViewsTableModel.PRIMARY_ADDRESS)
              .toString();
      if (!priAddrString.isEmpty()) {
        priAddr = new CAddress(priAddrString, 16);
      }

      final String secAddrString =
          table
              .getValueAt(rowIndex, ExtendedMatchedFunctionViewsTableModel.SECONDARY_ADDRESS)
              .toString();
      if (!secAddrString.isEmpty()) {
        secAddr = new CAddress(secAddrString, 16);
      }
    } else if (table instanceof FunctionDiffViewsContainerTable
        || table instanceof FunctionDiffViewsTable) {
      Diff diff = table.getDiff();

      if (diff == null) {
        final AbstractFunctionDiffViewsTableModel tableModel =
            (AbstractFunctionDiffViewsTableModel) table.getTableModel();
        final int modelIndex = table.getModel().modelIndex(rowIndex);

        diff = tableModel.getDiffAt(modelIndex);
      }

      if (!diff.isLoaded()) {
        return null;
      }

      priAddr = diff.getCallGraph(ESide.PRIMARY).getNodes().get(0).getAddress();
      secAddr = diff.getCallGraph(ESide.SECONDARY).getNodes().get(0).getAddress();
    } else if (table instanceof UnmatchedFunctionViewsTable) {
      final IAddress addr =
          new CAddress(
              table.getValueAt(rowIndex, UnmatchedFunctionViewsTableModel.ADDRESS).toString(), 16);

      if (((UnmatchedFunctionViewsTable) table).getSide() == ESide.PRIMARY) {
        priAddr = addr;
      } else {
        secAddr = addr;
      }
    }

    return Pair.make(priAddr, secAddr);
  }

  private void displayPopupMenu(final MouseEvent event) {
    final int selectedIndex = getSelectionIndex(event);
    if (selectedIndex == -1) {
      return;
    }

    final int rowIndex = rowAtPoint(event.getPoint());
    final int columnIndex = columnAtPoint(event.getPoint());

    final JPopupMenu popupMenu = getPopupMenu(rowIndex, columnIndex);
    if (popupMenu != null) {
      popupMenu.show(this, event.getX(), event.getY());
    }
  }

  private int getSelectionIndex(final MouseEvent event) {
    return tableSorter.modelIndex(rowAtPoint(event.getPoint()));
  }

  protected abstract JPopupMenu getPopupMenu(int rowIndex, int columnIndex);

  protected int[] getSortSelectedRows() {
    final int[] rows = getSelectedRows();

    for (int i = 0; i < rows.length; i++) {
      rows[i] = tableSorter.modelIndex(rows[i]);
    }

    return rows;
  }

  protected abstract void handleDoubleClick(int row);

  public void addListener(final IViewsTableListener listener) {
    listeners.addListener(listener);
  }

  public void dispose() {
    removeMouseListener(mouseListener);
    getSelectionModel().removeListSelectionListener(selectionListener);

    model.dispose();
  }

  public WorkspaceTabPanelFunctions getController() {
    return controller;
  }

  public Diff getDiff() {
    return model.getDiff();
  }

  @Override
  public CTableSorter getModel() {
    return tableSorter;
  }

  public AbstractTableModel getTableModel() {
    return model;
  }

  /**
   * Returns the tool tip for the specified row. By default always returns {@code null}. Override in
   * descending classes.
   *
   * @param diff the current {@code Diff} object to use for rendering the tool tip data
   * @param row the row to obtain a tooltip for.
   * @return the tool tip
   */
  public String getToolTipForRow(final Diff diff, final int row) {
    return null;
  }

  public boolean hasSelection() {
    return getSelectedRowCount() > 0;
  }

  public void removeListener(final IViewsTableListener listener) {
    listeners.removeListener(listener);
  }

  public void sortColumn(final int columnId, final int state) {
    tableSorter.setSortingStatus(columnId, state);
  }

  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(final MouseEvent event) {
      if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2) {
        handleDoubleClick(getSelectionIndex(event));
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.getButton() == 3 && !event.isControlDown()) {
        final int clickIndex = rowAtPoint(event.getPoint());

        if (!((DefaultListSelectionModel) getSelectionModel()).isSelectedIndex(clickIndex)) {
          getSelectionModel().clearSelection();
        }

        ((DefaultListSelectionModel) getSelectionModel())
            .addSelectionInterval(clickIndex, clickIndex);
      }

      if (event.isPopupTrigger()) {
        displayPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        displayPopupMenu(event);
      }
    }
  }

  private class InternalSelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(final ListSelectionEvent e) {
      for (final IViewsTableListener listener : listeners) {
        listener.rowSelectionChanged(AbstractTable.this);
      }
    }
  }
}
