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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.components.SliderPanel;
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

/** Settings panel for graph proximity browsing. */
public class ProximityBrowsingPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 5;
  private static final int SLIDER_MIN = 0;
  private static final int SLIDER_MAX = 10;
  private static final int SLIDER_LABEL_WIDTH = 25;

  private final JComboBox<String> proximityBrowsing = new JComboBox<>();
  private final SliderPanel proximityBrowsingParentDepth =
      new SliderPanel(
          0, SLIDER_MIN, SLIDER_MAX, true, true, false, true, SLIDER_LABEL_WIDTH, ROW_HEIGHT);
  private final SliderPanel proximityBrowsingChildDepth =
      new SliderPanel(
          0, SLIDER_MIN, SLIDER_MAX, true, true, false, true, SLIDER_LABEL_WIDTH, ROW_HEIGHT);
  private final JFormattedTextField autoProximityBrowsingActivionThreshold =
      TextComponentUtils.addDefaultEditorActions(
          new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(5))));
  private final JFormattedTextField visibilityWarningThreshold =
      TextComponentUtils.addDefaultEditorActions(
          new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(5))));

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public ProximityBrowsingPanel(final String borderTitle, final ESettingsDialogType type) {
    this(borderTitle, type, null);
  }

  public ProximityBrowsingPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());

    checkNotNull(borderTitle);
    checkArgument(settings == null ^ type == ESettingsDialogType.GRAPH_VIEW_SETTINGS);

    dialogType = type;
    this.settings = settings;

    init(borderTitle);
  }

  private int getAutoProximityBrowsingActivationThreshold(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getAutoProximityBrowsingActivationThreshold();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getAutoProximityBrowsingActivationThreshold();
      default:
    }

    return settings.getProximitySettings().getAutoProximityBrowsingActivationThreshold();
  }

  private boolean getProximityBrowsing(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getProximityBrowsing();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getProximityBrowsing();
      default:
    }

    return settings.getProximitySettings().getProximityBrowsing();
  }

  private int getProximityBrowsingChildDepth(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getProximityBrowsingChildDepth();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getProximityBrowsingChildDepth();
      default:
    }

    return settings.getProximitySettings().getProximityBrowsingChildren();
  }

  private int getProximityBrowsingParentDepth(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getProximityBrowsingParentDepth();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getProximityBrowsingParentDepth();
      default:
    }

    return settings.getProximitySettings().getProximityBrowsingParents();
  }

  private int getVisibilityWarningThreshold(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getVisibilityWarningThreshold();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getVisibilityWarningThreshold();
      default:
    }

    return settings.getLayoutSettings().getVisibilityWarningThreshold();
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    proximityBrowsing.addItem("On");
    proximityBrowsing.addItem("Off");

    setCurrentValues();

    final int rows =
        dialogType != ESettingsDialogType.GRAPH_VIEW_SETTINGS ? NUMBER_OF_ROWS : NUMBER_OF_ROWS - 2;

    final JPanel panel = new JPanel(new GridLayout(rows, 1, 5, 5));
    panel.setBorder(new TitledBorder(borderTitle));

    if (dialogType != ESettingsDialogType.GRAPH_VIEW_SETTINGS) {
      panel.add(
          GuiUtils.createHorizontalNamedComponentPanel(
              "Automatic proximity browsing", LABEL_WIDTH, proximityBrowsing, ROW_HEIGHT));
    }

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Proximity browsing parent depth",
            LABEL_WIDTH,
            proximityBrowsingParentDepth,
            ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Proximity browsing child depth",
            LABEL_WIDTH,
            proximityBrowsingChildDepth,
            ROW_HEIGHT));

    if (dialogType != ESettingsDialogType.GRAPH_VIEW_SETTINGS) {
      panel.add(
          GuiUtils.createHorizontalNamedComponentPanel(
              "Automatic proximity browsing activation threshold",
              LABEL_WIDTH,
              autoProximityBrowsingActivionThreshold,
              ROW_HEIGHT));
    }
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Visibility Warning threshold", LABEL_WIDTH, visibilityWarningThreshold, ROW_HEIGHT));

    add(panel, BorderLayout.NORTH);
  }

  public void dispose() {
    proximityBrowsingParentDepth.dispose();
    proximityBrowsingChildDepth.dispose();
  }

  public int getAutoProximityBrowsingActivationThreshold() {
    return Integer.parseInt(autoProximityBrowsingActivionThreshold.getText());
  }

  public boolean getProximityBrowsing() {
    return proximityBrowsing.getSelectedIndex() == 0;
  }

  public int getProximityBrowsingChildDepth() {
    return proximityBrowsingChildDepth.getValue();
  }

  public int getProximityBrowsingParentDepth() {
    return proximityBrowsingParentDepth.getValue();
  }

  public int getVisibilityWarningThreshold() {
    return Integer.parseInt(visibilityWarningThreshold.getText());
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    proximityBrowsing.setSelectedIndex(getProximityBrowsing(config) ? 0 : 1);
    proximityBrowsingParentDepth.setValue(getProximityBrowsingParentDepth(config));
    proximityBrowsingChildDepth.setValue(getProximityBrowsingChildDepth(config));
    autoProximityBrowsingActivionThreshold.setText(
        Integer.toString(getAutoProximityBrowsingActivationThreshold(config)));
    visibilityWarningThreshold.setText(Integer.toString(getVisibilityWarningThreshold(config)));
  }
}
