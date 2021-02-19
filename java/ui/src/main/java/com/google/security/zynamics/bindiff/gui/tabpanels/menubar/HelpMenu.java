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

package com.google.security.zynamics.bindiff.gui.tabpanels.menubar;

import com.google.security.zynamics.bindiff.BinDiff;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelFunctions;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.system.SystemHelpers;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/** The classic "Help" menu. Also allows to check for updates. */
public class HelpMenu extends JMenu {
  public HelpMenu(final TabPanelFunctions controller) {
    super("Help");
    setMnemonic('H');

    final JMenuItem helpMenuItem =
        GuiUtils.buildMenuItem(
            "Help Contents",
            'H',
            KeyEvent.VK_F1,
            0,
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                controller.showHelp();
              }
            });

    final JMenuItem reportABugMenuItem =
        GuiUtils.buildMenuItem(
            "Report a Bug",
            'R',
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                controller.reportABug();
              }
            });

    final JMenuItem checkForUpdatesMenuItem =
        GuiUtils.buildMenuItem(
            "Check for Updates...",
            'U',
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                controller.checkForUpdates();
              }
            });

    final JMenuItem aboutMenuItem =
        !SystemHelpers.isRunningMacOSX() || !BinDiff.isDesktopIntegrationDone()
            ? GuiUtils.buildMenuItem(
                "About",
                'A',
                new AbstractAction() {
                  @Override
                  public void actionPerformed(ActionEvent e) {
                    controller.showAboutDialog();
                  }
                })
            : null;

    add(helpMenuItem);
    add(new JSeparator());
    add(reportABugMenuItem);
    add(checkForUpdatesMenuItem);
    if (aboutMenuItem != null) {
      add(new JSeparator());
      add(aboutMenuItem);
    }
  }
}
