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

package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.enums.EGraphLayout;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.ESettingsDialogType;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public final class LayoutingPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 2;

  private final JComboBox<String> initalLayout = new JComboBox<>();
  private final JComboBox<String> autoLayouting = new JComboBox<>();

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public LayoutingPanel(final String borderTitle, final ESettingsDialogType type) {
    this(borderTitle, type, null);
  }

  public LayoutingPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());

    checkNotNull(borderTitle);
    checkArgument(settings == null ^ type == ESettingsDialogType.GRAPH_VIEW_SETTINGS);

    dialogType = type;
    this.settings = settings;

    init(borderTitle);
  }

  private boolean getAutoLayouting(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getAutoLayouting();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getAutoLayouting();
      default:
    }

    return settings.getLayoutSettings().getAutomaticLayouting();
  }

  private EGraphLayout getDefaultGraphLayout(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getDefaultGraphLayout();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getDefaultGraphLayout();
      default:
    }

    return settings.getLayoutSettings().getDefaultGraphLayout();
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    initalLayout.addItem("Hierarchical");
    initalLayout.addItem("Orthogonal");
    initalLayout.addItem("Circular");
    autoLayouting.addItem("On");
    autoLayouting.addItem("Off");

    setCurrentValues();

    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS, 1, 5, 5));

    panel.setBorder(new TitledBorder(borderTitle));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Default layout", LABEL_WIDTH, initalLayout, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Automatic layouting", LABEL_WIDTH, autoLayouting, ROW_HEIGHT));

    add(panel, BorderLayout.NORTH);
  }

  public boolean getAutoLayouting() {
    return autoLayouting.getSelectedIndex() == 0;
  }

  public EGraphLayout getDefaultLayout() {
    return EGraphLayout.getEnum(initalLayout.getSelectedIndex());
  }

  public void getDefaultLayout(final EGraphLayout layout) {
    initalLayout.setSelectedIndex(EGraphLayout.getOrdinal(layout));
  }

  public void setAutoLayouting(final boolean autoLayout) {
    autoLayouting.setSelectedIndex(autoLayout ? 0 : 1);
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    initalLayout.setSelectedIndex(EGraphLayout.getOrdinal(getDefaultGraphLayout(config)));
    autoLayouting.setSelectedIndex(getAutoLayouting(config) ? 0 : 1);
  }
}
