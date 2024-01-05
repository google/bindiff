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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseFunctionDiffsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseFunctionDiffsViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.LoadFunctionDiffsAction;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class FunctionDiffContainerNodePopupMenu extends JPopupMenu {
  public FunctionDiffContainerNodePopupMenu(final WorkspaceTabPanelFunctions controller) {
    checkNotNull(controller);

    add(
        GuiUtils.buildMenuItem(
            "Open Function Diffs",
            new LoadFunctionDiffsAction(controller),
            isOpenDiffsEnabled(controller)));

    add(
        GuiUtils.buildMenuItem(
            "Close Function Diffs",
            new CloseFunctionDiffsAction(controller),
            isCloseDiffsEnabled(controller)));

    add(new JSeparator());

    add(
        GuiUtils.buildMenuItem(
            "Close Function Diff Views",
            new CloseFunctionDiffsViewsAction(controller),
            isCloseViewsEnabled(controller)));
  }

  private boolean isCloseDiffsEnabled(final WorkspaceTabPanelFunctions controller) {
    for (final Diff diff : controller.getWorkspace().getDiffList(true)) {
      if (diff.isLoaded()) {
        return true;
      }
    }

    return false;
  }

  private boolean isCloseViewsEnabled(final WorkspaceTabPanelFunctions controller) {
    final TabPanelManager tabPanelManager =
        controller.getMainWindow().getController().getTabPanelManager();
    return tabPanelManager.getViewTabPanels(true).size() > 0;
  }

  private boolean isOpenDiffsEnabled(final WorkspaceTabPanelFunctions controller) {
    for (final Diff diff : controller.getWorkspace().getDiffList(true)) {
      if (!diff.isLoaded()) {
        return true;
      }
    }

    return false;
  }
}
