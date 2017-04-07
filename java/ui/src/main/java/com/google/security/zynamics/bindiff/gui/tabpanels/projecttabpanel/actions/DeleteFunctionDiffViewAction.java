package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.AllFunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.FunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.FunctionDiffViewsContainerNodeContextPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.FunctionDiffViewsNodeContextPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsContainerTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsTable;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;

public class DeleteFunctionDiffViewAction extends AbstractAction {
  private final AbstractTable table;
  private final int hitRowIndex;

  public DeleteFunctionDiffViewAction(final AbstractTable table, final int hitRowIndex) {
    this.table = Preconditions.checkNotNull(table);
    this.hitRowIndex = hitRowIndex;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final WorkspaceTabPanelFunctions controller = table.getController();

    final Diff diff = AbstractTable.getRowDiff(table, hitRowIndex);
    final List<Diff> diffs = new ArrayList<>();
    diffs.add(diff);
    if (controller.deleteFunctionDiffs(diffs)) {
      final WorkspaceTree tree = controller.getWorkspaceTree();

      if (table instanceof FunctionDiffViewsContainerTable) {
        ((FunctionDiffViewsContainerTable) table).removeRow(diff);

        final AllFunctionDiffViewsNode parent =
            (AllFunctionDiffViewsNode) tree.getModel().getRoot().getChildAt(0);
        for (int index = 0; index < parent.getChildCount(); ++index) {
          final FunctionDiffViewsNode node = (FunctionDiffViewsNode) parent.getChildAt(index);
          final FunctionDiffViewsNodeContextPanel component = node.getComponent();
          final FunctionDiffViewsTable table =
              (FunctionDiffViewsTable) component.getTables().get(0);
          table.removeRow(diff);
        }
      } else if (table instanceof FunctionDiffViewsTable) {
        ((FunctionDiffViewsTable) table).removeRow(diff);

        final AllFunctionDiffViewsNode node =
            (AllFunctionDiffViewsNode) tree.getModel().getRoot().getChildAt(0);
        final FunctionDiffViewsContainerNodeContextPanel component = node.getComponent();
        final FunctionDiffViewsContainerTable table =
            (FunctionDiffViewsContainerTable) component.getTables().get(0);
        table.removeRow(diff);
      } else {
        throw new IllegalArgumentException(
            "Table must be an instance of FunctionDiffViewsContainerTable or FunctionDiffViewsTable.");
      }
    }
  }
}
