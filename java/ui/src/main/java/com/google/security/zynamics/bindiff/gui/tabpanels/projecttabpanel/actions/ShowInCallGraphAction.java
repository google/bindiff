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

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;

public class ShowInCallGraphAction extends AbstractAction {
  private final AbstractTable table;

  public ShowInCallGraphAction(final AbstractTable table) {
    this.table = checkNotNull(table);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Set<Pair<IAddress, IAddress>> viewAddrPairs = new HashSet<>();
    for (final int row : table.getSelectedRows()) {
      viewAddrPairs.add(AbstractTable.getViewAddressPair(table, row));
    }

    final Diff diff = table.getDiff();
    table.getController().showInCallGraph(diff, viewAddrPairs);
  }
}
