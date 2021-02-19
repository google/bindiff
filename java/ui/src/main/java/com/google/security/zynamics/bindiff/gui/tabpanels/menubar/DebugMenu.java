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

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.DebugConfigItem;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelFunctions;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JToggleButton.ToggleButtonModel;

/** Top-level menu containing debugging options that can be enabled at runtime. */
public class DebugMenu extends JMenu {
  public DebugMenu(final TabPanelFunctions controller) {
    super("Debug");

    final JCheckBoxMenuItem showSuperGraphMenuItem = new JCheckBoxMenuItem("Show Super Graph");
    showSuperGraphMenuItem.setMnemonic('S');
    showSuperGraphMenuItem.setModel(
        new ToggleButtonModel() {
          final DebugConfigItem debugSettings = BinDiffConfig.getInstance().getDebugSettings();

          @Override
          public void setSelected(boolean b) {
            super.setSelected(b);
            debugSettings.setShowSuperGraph(b);
          }

          @Override
          public boolean isSelected() {
            return debugSettings.getShowSuperGraph();
          }
        });

    add(showSuperGraphMenuItem);
  }
}
