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

package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.enums.EGraphSynchronization;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.ESettingsDialogType;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.sliders.DoubleLabeledSlider;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class MiscPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 4;

  private final JCheckBox viewSynchronization = new JCheckBox();

  private final JCheckBox layoutAnimation = new JCheckBox();
  private final DoubleLabeledSlider animationSpeed =
      new DoubleLabeledSlider("  Slow  ", "  Fast  ", 1, 10, false, new LineBorder(Color.GRAY));

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public MiscPanel(final String borderTitle, final ESettingsDialogType type) {
    this(borderTitle, type, null);
  }

  public MiscPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());

    Preconditions.checkNotNull(borderTitle);
    Preconditions.checkArgument(settings == null ^ type == ESettingsDialogType.GRAPH_VIEW_SETTINGS);

    dialogType = type;
    this.settings = settings;

    animationSpeed.setInverted(true);

    init(borderTitle);
  }

  private int getAnimationSpeed(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getAnimationSpeed();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getAnimationSpeed();
      default:
    }

    return settings.getDisplaySettings().getAnimationSpeed();
  }

  private boolean getViewSynchronization(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getViewSynchronization();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getViewSynchronization();
      default:
    }

    return settings.getGraphSyncMode() == EGraphSynchronization.SYNC;
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    setCurrentValues();

    final int rows =
        dialogType != ESettingsDialogType.GRAPH_VIEW_SETTINGS ? NUMBER_OF_ROWS : NUMBER_OF_ROWS - 1;

    final JPanel panel = new JPanel(new GridLayout(rows, 1, 5, 5));
    panel.setBorder(new TitledBorder(borderTitle));

    if (dialogType != ESettingsDialogType.GRAPH_VIEW_SETTINGS) {
      panel.add(
          GuiUtils.createHorizontalNamedComponentPanel(
              "Views synchronization", LABEL_WIDTH, viewSynchronization, ROW_HEIGHT));
    }

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Layout animation", LABEL_WIDTH, layoutAnimation, ROW_HEIGHT));
    final JPanel animationSpeedPanel =
        GuiUtils.createHorizontalNamedComponentPanel(
            "Animation speed", LABEL_WIDTH, animationSpeed, ROW_HEIGHT);
    layoutAnimation.addItemListener(e -> animationSpeed.setEnabled(layoutAnimation.isSelected()));
    panel.add(animationSpeedPanel);

    add(panel, BorderLayout.NORTH);
  }

  public int getAnimationSpeed() {
    return layoutAnimation.isSelected() ? animationSpeed.getValue() : 0;
  }

  public boolean getViewSynchronization() {
    return viewSynchronization.isSelected();
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    viewSynchronization.setSelected(getViewSynchronization(config));

    final int speed = getAnimationSpeed(config);
    layoutAnimation.setSelected(speed > 0);
    animationSpeed.setValue(speed);
  }
}
