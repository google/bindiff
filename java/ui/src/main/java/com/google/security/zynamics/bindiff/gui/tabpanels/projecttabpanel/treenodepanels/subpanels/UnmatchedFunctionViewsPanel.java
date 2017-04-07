package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.subpanels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.IUnmatchedFunctionsViewsTableListener;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.UnmatchedFunctionViewsTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.UnmatchedFunctionViewsTableModel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class UnmatchedFunctionViewsPanel extends JPanel {
  private final Diff diff;
  private final ESide side;
  private final UnmatchedFunctionViewsTable unmatchedFunctionsTable;
  private final UnmatchedFunctionViewsFilterPanel filterPanel;
  private final InternalViewsTableListener tableListener = new InternalViewsTableListener();
  private final UnmatchedFunctionViewsTableModel unmatchedFunctionsTableModel;

  public UnmatchedFunctionViewsPanel(
      final Diff diff, final WorkspaceTabPanelFunctions controller, final ESide side) {
    super(new BorderLayout());

    this.diff = Preconditions.checkNotNull(diff);
    this.side = Preconditions.checkNotNull(side);

    unmatchedFunctionsTableModel = new UnmatchedFunctionViewsTableModel(diff, side, true);
    unmatchedFunctionsTable =
        new UnmatchedFunctionViewsTable(
            unmatchedFunctionsTableModel, Preconditions.checkNotNull(controller));

    filterPanel = new UnmatchedFunctionViewsFilterPanel(unmatchedFunctionsTable, side);

    unmatchedFunctionsTable.addListener(tableListener);
    unmatchedFunctionsTableModel.addListener(tableListener);

    init();
  }

  private void init() {
    setBorder(new TitledBorder(""));
    updateBorderTitle();

    final JScrollPane scrollpane = new JScrollPane(unmatchedFunctionsTable);
    final JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.add(filterPanel, BorderLayout.NORTH);
    tablePanel.add(scrollpane, BorderLayout.CENTER);

    add(tablePanel, BorderLayout.CENTER);
  }

  private void updateBorderTitle() {
    ((TitledBorder) getBorder())
        .setTitle(
            String.format(
                "%d / %d %s Unmatched Functions",
                unmatchedFunctionsTableModel.getRowCount(),
                diff.getMatches().getSizeOfUnmatchedFunctions(side),
                side == ESide.PRIMARY ? "Primary" : "Secondary"));

    updateUI();
  }

  public List<AbstractTable> getTables() {
    final List<AbstractTable> list = new ArrayList<>();
    list.add(unmatchedFunctionsTable);

    return list;
  }

  private class InternalViewsTableListener implements IUnmatchedFunctionsViewsTableListener {
    @Override
    public void rowSelectionChanged(final UnmatchedFunctionViewsTable table) {
      // do nothing
    }

    @Override
    public void tableDataChanged(final UnmatchedFunctionViewsTableModel model) {
      updateBorderTitle();
    }
  }
}
