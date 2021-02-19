// Copyright 2011-2021 Google LLC
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

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;

public class CloseDiffAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;

  public CloseDiffAction(final WorkspaceTabPanelFunctions controller) {
    this.controller = checkNotNull(controller);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Set<Diff> diffSet = new HashSet<>();
    diffSet.add(controller.getSelectedDiff());
    controller.closeDiffs(diffSet);
  }
}
