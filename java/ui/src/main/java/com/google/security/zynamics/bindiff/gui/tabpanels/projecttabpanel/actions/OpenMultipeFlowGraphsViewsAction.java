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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.Triple;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public class OpenMultipeFlowGraphsViewsAction extends AbstractAction {
  private static final int OPEN_VIEWS_WARNING_THRESHOLD = 10;

  private final AbstractTable table;

  public OpenMultipeFlowGraphsViewsAction(final AbstractTable table) {
    this.table = checkNotNull(table);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final WorkspaceTabPanelFunctions controller = table.getController();
    final LinkedHashSet<Triple<Diff, IAddress, IAddress>> viewsLoadData = new LinkedHashSet<>();

    final int[] selectedRowIndicies = table.getSelectedRows();

    final LinkedHashSet<Diff> diffsToLoad = new LinkedHashSet<>();
    for (int index = 0; index < selectedRowIndicies.length; ++index) {
      final int selectedRow = selectedRowIndicies[index];
      final Diff diff = AbstractTable.getRowDiff(table, selectedRow);

      if (!diff.isLoaded()) {
        diffsToLoad.add(diff);
      }
    }

    controller.loadFunctionDiffs(diffsToLoad);

    for (int index = 0; index < selectedRowIndicies.length; ++index) {
      final int selectedRow = selectedRowIndicies[index];
      final Diff diff = AbstractTable.getRowDiff(table, selectedRow);

      final Pair<IAddress, IAddress> viewAddrPair =
          AbstractTable.getViewAddressPair(table, selectedRow);

      viewsLoadData.add(Triple.make(diff, viewAddrPair.first(), viewAddrPair.second()));
    }

    int answer = JOptionPane.YES_OPTION;
    if (viewsLoadData.size() > OPEN_VIEWS_WARNING_THRESHOLD) {
      answer =
          CMessageBox.showYesNoQuestion(
              controller.getMainWindow(),
              String.format(
                  "This operation will open more than %d views. Continue?",
                  OPEN_VIEWS_WARNING_THRESHOLD));
    }

    if (answer == JOptionPane.YES_OPTION) {
      controller.openFlowGraphViews(controller.getMainWindow(), viewsLoadData);
    }
  }
}
