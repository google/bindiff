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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.menubar;

import com.google.security.zynamics.bindiff.config.Config;
import com.google.security.zynamics.bindiff.gui.tabpanels.menubar.DebugMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.menubar.HelpMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

public class ViewMenuBar extends JMenuBar {
  private final JMenu viewMenu;
  private final JMenu modeMenu;
  private final JMenu graphsMenu;

  public ViewMenuBar(final ViewTabPanelFunctions controller) {
    viewMenu = add(new ViewMenu(controller));
    modeMenu = add(new ModesMenu(controller));
    graphsMenu = add(new GraphsMenu(controller));
    add(new SelectionMenu(controller));
    add(new SearchMenu(controller));
    add(new WindowMenu(controller));
    if (Config.getInstance().getPreferencesOrBuilder().getDebugOrBuilder().getShowDebugMenu()) {
      add(new DebugMenu(controller));
    }
    add(new HelpMenu(controller));

    removeKeyBindings();
  }

  private void removeKeyBindings() {
    final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    if (inputMap == null) {
      return;
    }
    final Object action = inputMap.get(KeyStroke.getKeyStroke("F10"));
    final ActionMap actionMap = getActionMap();
    if (actionMap == null || actionMap.getParent() == null) {
      return;
    }
    actionMap.getParent().remove(action);
  }

  public void dispose() {
    ((ViewMenu) viewMenu).dispose();
    ((ModesMenu) modeMenu).dispose();
    ((GraphsMenu) graphsMenu).dispose();
  }
}
