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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.menubar;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ResetDefaultPerspectiveAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleGraphsPerspectiveAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.TogglePrimaryPerspectiveAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleSecondaryPerspectiveAction;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class WindowMenu extends JMenu {

  public WindowMenu(final ViewTabPanelFunctions controller) {
    super("Window");
    setMnemonic('W');

    checkNotNull(controller);

    final JMenuItem togglePrimaryPerspective =
        GuiUtils.buildMenuItem(
            "Show/Hide Primary",
            'P',
            KeyEvent.VK_F9,
            0,
            new TogglePrimaryPerspectiveAction(controller));

    final JMenuItem toggleSecondaryPerspective =
        GuiUtils.buildMenuItem(
            "Show/Hide Secondary",
            'S',
            KeyEvent.VK_F10,
            0,
            new ToggleSecondaryPerspectiveAction(controller));

    final JMenuItem toggleGraphsPerspective =
        GuiUtils.buildMenuItem(
            "Show/Hide Overviews",
            'G',
            KeyEvent.VK_F11,
            0,
            new ToggleGraphsPerspectiveAction(controller));

    final JMenuItem resetDefaultPerspective =
        GuiUtils.buildMenuItem(
            "Reset Window Layout",
            'R',
            KeyEvent.VK_F12,
            0,
            new ResetDefaultPerspectiveAction(controller));

    add(togglePrimaryPerspective);
    add(toggleSecondaryPerspective);
    add(toggleGraphsPerspective);
    add(new JSeparator());
    add(resetDefaultPerspective);
  }
}
