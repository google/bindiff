package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.FunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.FunctionDiffViewsNodeContextPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsTable;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.CFileUtils;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;

public class DeleteFunctionDiffViewsAction extends AbstractAction {
  private final FunctionDiffViewsNode viewsNode;

  public DeleteFunctionDiffViewsAction(final FunctionDiffViewsNode viewsNode) {
    this.viewsNode = Preconditions.checkNotNull(viewsNode);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final FunctionDiffViewsNodeContextPanel component = viewsNode.getComponent();
    final FunctionDiffViewsTable table = (FunctionDiffViewsTable) component.getTables().get(0);

    final WorkspaceTabPanelFunctions controller = table.getController();

    final List<Diff> diffsToDelete = new ArrayList<>();
    for (int row = 0; row < table.getRowCount(); ++row) {
      diffsToDelete.add(AbstractTable.getRowDiff(table, row));
    }

    controller.deleteFunctionDiffs(diffsToDelete);

    try {
      if (viewsNode.getViewDirectory().exists()) {
        CFileUtils.deleteDirectory(viewsNode.getViewDirectory());
      }
    } catch (final IOException e) {
      Logger.logException(e, "Couldn't delete function diff's directory.");
      CMessageBox.showError(
          controller.getMainWindow(), "Couldn't delete function diff's directory.");
    }
  }
}
