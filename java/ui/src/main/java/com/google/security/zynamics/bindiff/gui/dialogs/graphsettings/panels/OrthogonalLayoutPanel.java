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

package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.enums.ELayoutOrientation;
import com.google.security.zynamics.bindiff.enums.EOrthogonalLayoutStyle;
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

/** Settings panel for orthogonal layouts. */
public class OrthogonalLayoutPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 3;

  private final JComboBox<ELayoutOrientation> orientation =
      new JComboBox<>(ELayoutOrientation.values());
  private final JComboBox<EOrthogonalLayoutStyle> layoutStyle =
      new JComboBox<>(EOrthogonalLayoutStyle.values());
  private final JFormattedTextField minimumNodeDistance =
      TextComponentUtils.addDefaultEditorActions(
          new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(3))));

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public OrthogonalLayoutPanel(final String borderTitle, final ESettingsDialogType type) {
    this(borderTitle, type, null);
  }

  public OrthogonalLayoutPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());

    checkNotNull(borderTitle);
    checkArgument(settings == null ^ type == ESettingsDialogType.GRAPH_VIEW_SETTINGS);

    dialogType = type;
    this.settings = settings;

    init(borderTitle);
  }

  private int getMinimumNodeDistance(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getOrthogonalMinimumNodeDistance();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getOrthogonalMinimumNodeDistance();
      default:
    }

    return (int) settings.getLayoutSettings().getMinimumOrthogonalNodeDistance();
  }

  private EOrthogonalLayoutStyle getOrthogonalLayoutStyle(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getOrthogonalLayoutStyle();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getOrthogonalLayoutStyle();
      default:
    }

    return settings.getLayoutSettings().getOrthogonalLayoutStyle();
  }

  private ELayoutOrientation getOrthogonalOrientation(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getOrthogonalOrientation();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getOrthogonalOrientation();
      default:
    }

    return settings.getLayoutSettings().getOrthogonalLayoutOrientation();
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    setCurrentValues();

    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS, 1, 5, 5));
    panel.setBorder(new TitledBorder(borderTitle));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Layout Style", LABEL_WIDTH, layoutStyle, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Layout orientation", LABEL_WIDTH, orientation, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Minimum node distance", LABEL_WIDTH, minimumNodeDistance, ROW_HEIGHT));

    add(panel, BorderLayout.NORTH);
  }

  public int getMinimumNodeDistance() {
    return Integer.parseInt(minimumNodeDistance.getText());
  }

  public EOrthogonalLayoutStyle getOrthogonalLayoutStyle() {
    return (EOrthogonalLayoutStyle) layoutStyle.getSelectedItem();
  }

  public ELayoutOrientation getOrthogonalOrientation() {
    return (ELayoutOrientation) orientation.getSelectedItem();
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    layoutStyle.setSelectedItem(getOrthogonalLayoutStyle(config));
    orientation.setSelectedItem(getOrthogonalOrientation(config));
    minimumNodeDistance.setText(Integer.toString(getMinimumNodeDistance(config)));
  }
}
