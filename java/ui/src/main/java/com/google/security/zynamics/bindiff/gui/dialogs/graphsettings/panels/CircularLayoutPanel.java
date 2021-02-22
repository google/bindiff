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
import com.google.security.zynamics.bindiff.enums.ECircularLayoutStyle;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.ESettingsDialogType;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.CDecFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultFormatterFactory;

/** Settings panel for circular graph layouts. */
public class CircularLayoutPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 2;

  private final JComboBox<String> layoutStyle = new JComboBox<>();
  private final JFormattedTextField minimumNodeDistance =
      TextComponentUtils.addDefaultEditorActions(
          new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(3))));

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public CircularLayoutPanel(final String borderTitle, final ESettingsDialogType type) {
    this(borderTitle, type, null);
  }

  public CircularLayoutPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());

    checkNotNull(borderTitle);
    checkArgument(settings == null ^ type == ESettingsDialogType.GRAPH_VIEW_SETTINGS);

    dialogType = type;
    this.settings = settings;

    init(borderTitle);
  }

  private ECircularLayoutStyle getCircularLayoutStyle(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getCircularLayoutStyle();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getCircularLayoutStyle();
      default:
    }

    return settings.getLayoutSettings().getCircularLayoutStyle();
  }

  private int getMinimumNodeDistance(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getCircularMinimumNodeDistance();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getCircularMinimumNodeDistance();
      default:
    }

    return (int) settings.getLayoutSettings().getMinimumCircularNodeDistance();
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    layoutStyle.addItem("Compact");
    layoutStyle.addItem("Isolated");
    layoutStyle.addItem("Single Cycle");
    setCurrentValues();

    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS, 1, 5, 5));

    panel.setBorder(new TitledBorder(borderTitle));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Layout style", LABEL_WIDTH, layoutStyle, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Preferred node distance", LABEL_WIDTH, minimumNodeDistance, ROW_HEIGHT));

    add(panel, BorderLayout.NORTH);
  }

  public ECircularLayoutStyle getCircularLayoutStyle() {
    return ECircularLayoutStyle.values()[layoutStyle.getSelectedIndex()];
  }

  public int getMinimumNodeDistance() {
    return Integer.parseInt(minimumNodeDistance.getText());
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    layoutStyle.setSelectedIndex(getCircularLayoutStyle(config).ordinal());

    minimumNodeDistance.setText(Integer.toString(getMinimumNodeDistance(config)));
  }
}
