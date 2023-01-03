// Copyright 2011-2023 Google LLC
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
import com.google.security.zynamics.bindiff.enums.EMouseAction;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.ESettingsDialogType;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import com.google.security.zynamics.zylib.gui.sliders.DoubleLabeledSlider;
import com.google.security.zynamics.zylib.gui.zygraph.MouseWheelAction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class ControlsPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 4;

  private final JComboBox<String> showScrollbars = new JComboBox<>();
  private final JComboBox<String> mouseWheelBehavior = new JComboBox<>();
  private final DoubleLabeledSlider zoomSensitivity =
      new DoubleLabeledSlider("  Low  ", "  High  ", 1, 10, false, new LineBorder(Color.GRAY));
  private final DoubleLabeledSlider scrollSensitivity =
      new DoubleLabeledSlider("  Low  ", "  High  ", 1, 10, false, new LineBorder(Color.GRAY));

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public ControlsPanel(final String borderTitle, final ESettingsDialogType type) {
    this(borderTitle, type, null);
  }

  public ControlsPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());

    checkNotNull(borderTitle);
    checkArgument(settings == null ^ type == ESettingsDialogType.GRAPH_VIEW_SETTINGS);

    dialogType = type;
    this.settings = settings;

    zoomSensitivity.setInverted(true);
    scrollSensitivity.setInverted(true);

    init(borderTitle);
  }

  private EMouseAction getMouseWheelBehaviour(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getMouseWheelAction();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getMouseWheelAction();
      default:
    }

    return settings.getMouseSettings().getMouseWheelAction() == MouseWheelAction.ZOOM
        ? EMouseAction.ZOOM
        : EMouseAction.SCROLL;
  }

  private int getScrollSensitivity(final BinDiffConfig config) {

    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getScrollSensitivity();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getScrollSensitivity();
      default:
    }

    return settings.getMouseSettings().getScrollSensitivity();
  }

  private boolean getShowScrollbars(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getShowScrollbars();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getShowScrollbars();
      default:
    }

    return settings.getShowScrollbars();
  }

  private int getZoomSensitivity(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALL_GRAPH_SETTING:
        return config.getInitialCallGraphSettings().getZoomSensitivity();
      case INITIAL_FLOW_GRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getZoomSensitivity();
      default:
    }

    return settings.getMouseSettings().getZoomSensitivity();
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    showScrollbars.addItem("Always");
    showScrollbars.addItem("Never");
    mouseWheelBehavior.addItem("Zoom");
    mouseWheelBehavior.addItem("Scroll");
    setCurrentValues();

    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS, 1, 5, 5));
    panel.setBorder(new TitledBorder(borderTitle));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Show scrollbars", LABEL_WIDTH, showScrollbars, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Mouse wheel action", LABEL_WIDTH, mouseWheelBehavior, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Zoom sensitivity", LABEL_WIDTH, zoomSensitivity, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Scroll sensitivity", LABEL_WIDTH, scrollSensitivity, ROW_HEIGHT));

    add(panel, BorderLayout.NORTH);
  }

  public EMouseAction getMouseWheelBehavior() {
    return mouseWheelBehavior.getSelectedIndex() == 0 ? EMouseAction.ZOOM : EMouseAction.SCROLL;
  }

  public int getScrollSensitivity() {
    return scrollSensitivity.getValue();
  }

  public boolean getShowScrollbars() {
    return showScrollbars.getSelectedIndex() == 0;
  }

  public int getZoomSensitivity() {
    return zoomSensitivity.getValue();
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    showScrollbars.setSelectedIndex(getShowScrollbars(config) ? 0 : 1);
    mouseWheelBehavior.setSelectedIndex(
        getMouseWheelBehaviour(config) == EMouseAction.ZOOM ? 0 : 1);
    zoomSensitivity.setValue(getZoomSensitivity(config));
    scrollSensitivity.setValue(getScrollSensitivity(config));
  }
}
