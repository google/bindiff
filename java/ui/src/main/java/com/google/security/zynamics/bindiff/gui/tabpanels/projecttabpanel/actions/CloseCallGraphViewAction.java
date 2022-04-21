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

import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;

public class CloseCallGraphViewAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;
  private final Diff diff;

  public CloseCallGraphViewAction(final WorkspaceTabPanelFunctions controller, final Diff diff) {
    this.controller = checkNotNull(controller);
    this.diff = checkNotNull(diff);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final TabPanelManager tabManager =
        controller.getMainWindow().getController().getTabPanelManager();
    final ViewTabPanel panel = tabManager.getTabPanel(null, null, diff);

    if (panel != null) {
      final Set<ViewTabPanel> views = new HashSet<>();
      views.add(panel);
      controller.closeViews(views);
    }
  }
}
