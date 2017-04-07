package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsContainerTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsContainerTableModel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class FunctionDiffViewsContainerNodeContextPanel extends AbstractTreeNodeContextPanel {
  private final FunctionDiffViewsContainerTable allFunctionDiffsViewsTable;

  private final TableModelListener tableModelListener = new InternalTableModelListener();

  public FunctionDiffViewsContainerNodeContextPanel(final WorkspaceTabPanelFunctions controller) {
    final List<Diff> singleFlowgraphViewDiffList =
        Preconditions.checkNotNull(controller).getWorkspace().getDiffList(true);

    final FunctionDiffViewsContainerTableModel allFunctionDiffViewsTableModel =
        new FunctionDiffViewsContainerTableModel(singleFlowgraphViewDiffList);
    allFunctionDiffsViewsTable =
        new FunctionDiffViewsContainerTable(allFunctionDiffViewsTableModel, controller);

    allFunctionDiffViewsTableModel.addTableModelListener(tableModelListener);

    init();
  }

  private JPanel createTablePanel() {
    final JScrollPane scrollpane = new JScrollPane(allFunctionDiffsViewsTable);
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
            String.format(
                "%d Single Function Diff Views", allFunctionDiffsViewsTable.getRowCount()));
  }

  public void dispose() {
    allFunctionDiffsViewsTable.getTableModel().removeTableModelListener(tableModelListener);
  }

  @Override
  public List<AbstractTable> getTables() {
    final List<AbstractTable> tables = new ArrayList<>();
    tables.add(allFunctionDiffsViewsTable);
    return tables;
  }

  private class InternalTableModelListener implements TableModelListener {
    @Override
    public void tableChanged(final TableModelEvent e) {
      updateBorderTitle();
    }
  }
}
