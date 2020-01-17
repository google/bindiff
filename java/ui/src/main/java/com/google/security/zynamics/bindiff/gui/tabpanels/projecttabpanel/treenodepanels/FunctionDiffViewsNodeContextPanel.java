// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsTableModel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class FunctionDiffViewsNodeContextPanel extends AbstractTreeNodeContextPanel {
  private final FunctionDiffViewsTable functionDiffViewsTable;

  private final TableModelListener tableModelListener = new InternalTableModelListener();

  public FunctionDiffViewsNodeContextPanel(
      final WorkspaceTabPanelFunctions controller, final List<Diff> functionDiffList) {
    final FunctionDiffViewsTableModel singleFlowgraphViewsTableModel =
        new FunctionDiffViewsTableModel(functionDiffList);
    functionDiffViewsTable = new FunctionDiffViewsTable(singleFlowgraphViewsTableModel, controller);
    functionDiffViewsTable.getTableModel().addTableModelListener(tableModelListener);

    init();
  }

  private JPanel createTablePanel() {
    final JScrollPane scrollpane = new JScrollPane(functionDiffViewsTable);
    final JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.add(scrollpane, BorderLayout.CENTER);

    return tablePanel;
  }

  private void init() {
    setBorder(new TitledBorder(""));
    add(createTablePanel(), BorderLayout.CENTER);

    updateBorderTitle();
  }

  private void updateBorderTitle() {
    ((TitledBorder) getBorder())
        .setTitle(
            String.format("%d Single Function Diff Views", functionDiffViewsTable.getRowCount()));
  }

  public void dispose() {
    functionDiffViewsTable.getTableModel().removeTableModelListener(tableModelListener);
  }

  public FunctionDiffViewsTableModel getFunctionViewsTableModel() {
    return (FunctionDiffViewsTableModel) functionDiffViewsTable.getTableModel();
  }

  @Override
  public List<AbstractTable> getTables() {
    final List<AbstractTable> tables = new ArrayList<>();
    tables.add(functionDiffViewsTable);
    return tables;
  }

  private class InternalTableModelListener implements TableModelListener {
    @Override
    public void tableChanged(final TableModelEvent e) {
      updateBorderTitle();
    }
  }
}
