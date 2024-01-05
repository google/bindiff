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

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.menubar;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EGraphSynchronization;
import com.google.security.zynamics.bindiff.graph.settings.GraphLayoutSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphProximityBrowsingSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettingsChangedListenerAdapter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SwitchToCombinedViewModeAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.SwitchToNormalViewModeAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleAutomaticLayoutAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleGraphSynchronizationAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleProximityBrowsingAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions.ToggleProximityFreezeModeAction;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

public class ModesMenu extends JMenu {
  private final JRadioButtonMenuItem normalViewMode;
  private final JRadioButtonMenuItem combinedViewMode;

  private final JCheckBoxMenuItem synchronizeGraphs;

  private final JCheckBoxMenuItem automaticLayout;
  private final JCheckBoxMenuItem proximityBrowsing;
  private final JCheckBoxMenuItem proximityFreezeMode;

  private InternalSettingsListener settingsListener = new InternalSettingsListener();

  private ViewTabPanelFunctions controller;

  public ModesMenu(final ViewTabPanelFunctions controller) {
    super("Mode");
    setMnemonic('M');

    final int CTRL_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    this.controller = checkNotNull(controller);

    normalViewMode =
        GuiUtils.buildRadioButtonMenuItem(
            "Normal View Mode",
            'N',
            KeyEvent.VK_1,
            CTRL_MASK,
            new SwitchToNormalViewModeAction(controller));

    combinedViewMode =
        GuiUtils.buildRadioButtonMenuItem(
            "Combined View Mode",
            'C',
            KeyEvent.VK_2,
            CTRL_MASK,
            new SwitchToCombinedViewModeAction(controller));

    synchronizeGraphs =
        GuiUtils.buildCheckBoxMenuItem(
            "Synchronize Graphs",
            'S',
            KeyEvent.VK_F8,
            0,
            new ToggleGraphSynchronizationAction(controller));

    automaticLayout =
        GuiUtils.buildCheckBoxMenuItem(
            "Automatic Layout",
            'A',
            KeyEvent.VK_F5,
            0,
            new ToggleAutomaticLayoutAction(controller));
    proximityBrowsing =
        GuiUtils.buildCheckBoxMenuItem(
            "Proximity Browsing",
            'P',
            KeyEvent.VK_F6,
            0,
            new ToggleProximityBrowsingAction(controller));
    proximityFreezeMode =
        GuiUtils.buildCheckBoxMenuItem(
            "Proximity Freeze-Mode",
            'F',
            KeyEvent.VK_F7,
            0,
            new ToggleProximityFreezeModeAction(controller));

    final ButtonGroup group = new ButtonGroup();
    normalViewMode.setSelected(true);
    group.add(normalViewMode);
    group.add(combinedViewMode);

    add(normalViewMode);
    add(combinedViewMode);
    add(new JSeparator());
    add(automaticLayout);
    add(proximityBrowsing);
    add(proximityFreezeMode);
    add(new JSeparator());
    add(synchronizeGraphs);

    final GraphSettings settings = controller.getGraphSettings();

    initStates(settings);

    settings.addListener(settingsListener);
  }

  private void initStates(final GraphSettings settings) {
    switch (settings.getDiffViewMode()) {
      case NORMAL_VIEW:
        normalViewMode.setSelected(true);
        break;
      case COMBINED_VIEW:
        combinedViewMode.setSelected(true);
        break;
      default:
    }

    automaticLayout.setSelected(settings.getLayoutSettings().getAutomaticLayouting());
    proximityBrowsing.setSelected(settings.getProximitySettings().getProximityBrowsing());
    proximityFreezeMode.setSelected(settings.getProximitySettings().getProximityBrowsingFrozen());

    synchronizeGraphs.setSelected(settings.getGraphSyncMode() == EGraphSynchronization.SYNC);
  }

  public void dispose() {
    controller.getGraphSettings().removeListener(settingsListener);
    settingsListener = null;
    controller = null;
  }

  private class InternalSettingsListener extends GraphSettingsChangedListenerAdapter {
    @Override
    public void autoLayoutChanged(final GraphLayoutSettings settings) {
      automaticLayout.setSelected(settings.getAutomaticLayouting());
    }

    @Override
    public void diffViewModeChanged(final GraphSettings settings) {
      switch (settings.getDiffViewMode()) {
        case NORMAL_VIEW:
          normalViewMode.setSelected(true);
          break;
        case COMBINED_VIEW:
          combinedViewMode.setSelected(true);
          break;
        default:
      }
    }

    @Override
    public void graphSyncChanged(final GraphSettings settings) {
      synchronizeGraphs.setSelected(settings.getGraphSyncMode() == EGraphSynchronization.SYNC);
    }

    @Override
    public void proximityBrowsingChanged(final GraphProximityBrowsingSettings settings) {
      proximityBrowsing.setSelected(settings.getProximityBrowsing());
    }

    @Override
    public void proximityBrowsingFrozenChanged(final GraphProximityBrowsingSettings settings) {
      proximityFreezeMode.setSelected(settings.getProximityBrowsingFrozen());
    }
  }
}
