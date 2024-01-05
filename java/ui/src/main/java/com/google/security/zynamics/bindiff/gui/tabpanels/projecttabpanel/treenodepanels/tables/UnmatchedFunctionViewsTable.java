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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup.FlowGraphViewsTablePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.BackgroundCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.FunctionTypeCellRenderer;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.tables.CTableSorter;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

public class UnmatchedFunctionViewsTable extends AbstractTable {

  private final ListenerProvider<IUnmatchedFunctionsViewsTableListener> listeners =
      new ListenerProvider<>();

  private final ESide side;

  public UnmatchedFunctionViewsTable(
      final UnmatchedFunctionViewsTableModel unmatchedModel,
      final WorkspaceTabPanelFunctions controller) {
    super(unmatchedModel, controller);

    sortColumn(UnmatchedFunctionViewsTableModel.TYPE, CTableSorter.ASCENDING);
    sortColumn(UnmatchedFunctionViewsTableModel.ADDRESS, CTableSorter.ASCENDING);

    side = unmatchedModel.getSide();

    init();

    getSelectionModel().addListSelectionListener(new InternalSelectionListener());
  }

  private void init() {
    final TableColumn address =
        getColumnModel().getColumn(UnmatchedFunctionViewsTableModel.ADDRESS);
    final TableColumn name =
        getColumnModel().getColumn(UnmatchedFunctionViewsTableModel.FUNCTION_NAME);
    final TableColumn type = getColumnModel().getColumn(UnmatchedFunctionViewsTableModel.TYPE);
    final TableColumn callers =
        getColumnModel().getColumn(UnmatchedFunctionViewsTableModel.CALLERS);
    final TableColumn calls = getColumnModel().getColumn(UnmatchedFunctionViewsTableModel.CALLEES);
    final TableColumn basicBlocks =
        getColumnModel().getColumn(UnmatchedFunctionViewsTableModel.BASICBLOCKS);
    final TableColumn jumps = getColumnModel().getColumn(UnmatchedFunctionViewsTableModel.JUMPS);
    final TableColumn instructions =
        getColumnModel().getColumn(UnmatchedFunctionViewsTableModel.INSTRUCTIONS);

    address.setMinWidth(60);
    name.setMinWidth(55);
    type.setMinWidth(35);
    callers.setMinWidth(40);
    calls.setMinWidth(75);
    basicBlocks.setMinWidth(40);
    jumps.setMinWidth(40);
    instructions.setMinWidth(40);

    address.setPreferredWidth(60);
    name.setPreferredWidth(200);
    type.setPreferredWidth(35);
    callers.setPreferredWidth(50);
    calls.setPreferredWidth(75);
    basicBlocks.setPreferredWidth(50);
    jumps.setPreferredWidth(50);
    instructions.setPreferredWidth(50);

    setRowHeight(GuiHelper.getMonospacedFontMetrics().getHeight() + 4);

    final BackgroundCellRenderer nameAndAddressRenderer =
        new BackgroundCellRenderer(
            GuiHelper.getMonospacedFont(), Colors.GRAY250, Colors.GRAY32, SwingConstants.LEFT);
    final BackgroundCellRenderer numberRenderer =
        new BackgroundCellRenderer(
            GuiHelper.getDefaultFont(), Colors.GRAY250, Colors.GRAY32, SwingConstants.LEFT);

    address.setCellRenderer(nameAndAddressRenderer);
    name.setCellRenderer(nameAndAddressRenderer);
    type.setCellRenderer(new FunctionTypeCellRenderer());
    callers.setCellRenderer(numberRenderer);
    basicBlocks.setCellRenderer(numberRenderer);
    jumps.setCellRenderer(numberRenderer);
    instructions.setCellRenderer(numberRenderer);
    calls.setCellRenderer(numberRenderer);
  }

  @Override
  protected JPopupMenu getPopupMenu(final int rowIndex, final int columnIndex) {
    return new FlowGraphViewsTablePopupMenu(this, rowIndex, columnIndex);
  }

  @Override
  protected void handleDoubleClick(final int row) {
    CAddress addr =
        new CAddress(
            (String) getTableModel().getValueAt(row, UnmatchedFunctionViewsTableModel.ADDRESS), 16);
    final IAddress primaryAddr = side == ESide.PRIMARY ? addr : null;
    final IAddress secondaryAddr = side == ESide.SECONDARY ? addr : null;

    final WorkspaceTabPanelFunctions controller = getController();
    controller.openFlowGraphView(controller.getMainWindow(), getDiff(), primaryAddr, secondaryAddr);
  }

  public void addListener(final IUnmatchedFunctionsViewsTableListener listener) {
    listeners.addListener(listener);
  }

  public ESide getSide() {
    return side;
  }

  public void removeListener(final IUnmatchedFunctionsViewsTableListener listener) {
    listeners.removeListener(listener);
  }

  private class InternalSelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(final ListSelectionEvent e) {
      for (final IUnmatchedFunctionsViewsTableListener listener : listeners) {
        listener.rowSelectionChanged(UnmatchedFunctionViewsTable.this);
      }
    }
  }
}
