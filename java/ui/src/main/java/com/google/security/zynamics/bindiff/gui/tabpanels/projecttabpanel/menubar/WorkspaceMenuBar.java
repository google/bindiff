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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.menubar;

import com.google.security.zynamics.bindiff.config.Config;
import com.google.security.zynamics.bindiff.gui.tabpanels.menubar.DebugMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.menubar.HelpMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class WorkspaceMenuBar extends JMenuBar {
  private final JMenu workspaceMenu;

  public WorkspaceMenuBar(final WorkspaceTabPanelFunctions controller) {
    workspaceMenu = add(new WorkspaceMenu(controller));
    add(new DiffMenu(controller));
    add(new SettingsMenu(controller));
    if (Config.getInstance().getPreferencesOrBuilder().getDebugOrBuilder().getShowDebugMenu()) {
      add(new DebugMenu(controller));
    }
    add(new HelpMenu(controller));
  }

  public String[] getRecentWorkspaces() {
    return ((WorkspaceMenu) workspaceMenu).getRecentWorkspaces();
  }
}
