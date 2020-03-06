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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.FunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.FunctionDiffViewsNodeContextPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsTable;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;

public class DeleteFunctionDiffViewsAction extends AbstractAction {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final FunctionDiffViewsNode viewsNode;

  public DeleteFunctionDiffViewsAction(final FunctionDiffViewsNode viewsNode) {
    this.viewsNode = checkNotNull(viewsNode);
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
        BinDiffFileUtils.deleteDirectory(viewsNode.getViewDirectory());
      }
    } catch (final IOException e) {
      logger.at(Level.SEVERE).withCause(e).log("Couldn't delete function diff's directory");
      CMessageBox.showError(
          controller.getMainWindow(), "Couldn't delete function diff's directory.");
    }
  }
}
