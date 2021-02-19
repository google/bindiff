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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.JumpToAddressAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SearchAction;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class SearchMenu extends JMenu {

  public SearchMenu(final ViewTabPanelFunctions controller) {
    super("Search");
    setMnemonic('A');

    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    checkNotNull(controller);

    final JMenuItem jumpToPrimaryAddress =
        GuiUtils.buildMenuItem(
            "Jump to Primary Address",
            'P',
            KeyEvent.VK_J,
            CTRL_MASK,
            new JumpToAddressAction(controller, ESide.PRIMARY));

    final JMenuItem jumpToSecondaryAddress =
        GuiUtils.buildMenuItem(
            "Jump to Secondary Address",
            'S',
            KeyEvent.VK_J,
            CTRL_MASK | InputEvent.SHIFT_DOWN_MASK,
            new JumpToAddressAction(controller, ESide.SECONDARY));

    final JMenuItem search =
        GuiUtils.buildMenuItem(
            "Search", 'S', KeyEvent.VK_F, CTRL_MASK, new SearchAction(controller));

    add(search);
    add(new JSeparator());
    add(jumpToPrimaryAddress);
    add(jumpToSecondaryAddress);
  }
}
