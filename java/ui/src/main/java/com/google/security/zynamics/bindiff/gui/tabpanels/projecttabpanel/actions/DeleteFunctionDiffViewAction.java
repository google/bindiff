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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

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
    this.table = checkNotNull(table);
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
            "Table must be an instance of FunctionDiffViewsContainerTable or"
                + " FunctionDiffViewsTable.");
      }
    }
  }
}
