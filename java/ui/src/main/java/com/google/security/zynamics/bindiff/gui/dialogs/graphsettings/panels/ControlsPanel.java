package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels;

import com.google.common.base.Preconditions;
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
  private final JComboBox<String> mouseWheelBehaviour = new JComboBox<>();
  private final DoubleLabeledSlider zoomSensitivity =
      new DoubleLabeledSlider(
          "  " + "Low" + "  ", "  " + "High" + "  ", 1, 10, false, new LineBorder(Color.GRAY));
  private final DoubleLabeledSlider scrollSensitivity =
      new DoubleLabeledSlider(
          "  " + "Low" + "  ", "  " + "High" + "  ", 1, 10, false, new LineBorder(Color.GRAY));

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public ControlsPanel(final String borderTitle, final ESettingsDialogType type) {
    super(new BorderLayout());
    Preconditions.checkNotNull(borderTitle);

    if (type == null || type == ESettingsDialogType.NON_INITIAL) {
      throw new IllegalArgumentException("Dialog type cannot be null or non-initial.");
    }

    dialogType = type;

    settings = null;

    zoomSensitivity.setInverted(true);
    scrollSensitivity.setInverted(true);

    init(borderTitle);
  }

  public ControlsPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());
    Preconditions.checkNotNull(borderTitle);

    if (type == null || type != ESettingsDialogType.NON_INITIAL) {
      throw new IllegalArgumentException("Dialog type cannot be null or not non-initial.");
    }

    dialogType = type;

    this.settings = settings;

    zoomSensitivity.setInverted(true);
    scrollSensitivity.setInverted(true);

    init(borderTitle);
  }

  private EMouseAction getMouseWheelBehaviour(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return EMouseAction.getEnum(config.getInitialCallgraphSettings().getMouseWheelAction());
      case INITIAL_FLOWGRAPH_SETTINGS:
        return EMouseAction.getEnum(config.getInitialFlowgraphSettings().getMouseWheelAction());
      default:
    }

    return settings.getMouseSettings().getMouseWheelAction() == MouseWheelAction.ZOOM
        ? EMouseAction.ZOOM
        : EMouseAction.SCROLL;
  }

  private int getScrollSensitivity(final BinDiffConfig config) {

    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return config.getInitialCallgraphSettings().getScrollSensitivity();
      case INITIAL_FLOWGRAPH_SETTINGS:
        return config.getInitialFlowgraphSettings().getScrollSensitivity();
      default:
    }

    return settings.getMouseSettings().getScrollSensitivity();
  }

  private boolean getShowScrollbars(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return config.getInitialCallgraphSettings().getShowScrollbars();
      case INITIAL_FLOWGRAPH_SETTINGS:
        return config.getInitialFlowgraphSettings().getShowScrollbars();
      default:
    }

    return settings.getShowScrollbars();
  }

  private int getZoomSensitivity(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return config.getInitialCallgraphSettings().getZoomSensitivity();
      case INITIAL_FLOWGRAPH_SETTINGS:
        return config.getInitialFlowgraphSettings().getZoomSensitivity();
      default:
    }

    return settings.getMouseSettings().getZoomSensitivity();
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    showScrollbars.addItem("Always");
    showScrollbars.addItem("Never");
    mouseWheelBehaviour.addItem("Zoom");
    mouseWheelBehaviour.addItem("Scroll");
    setCurrentValues();

    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS, 1, 5, 5));
    panel.setBorder(new TitledBorder(borderTitle));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Show scrollbars", LABEL_WIDTH, showScrollbars, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Mousewheel action", LABEL_WIDTH, mouseWheelBehaviour, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Zoom sensitivity", LABEL_WIDTH, zoomSensitivity, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Scroll sensitivity", LABEL_WIDTH, scrollSensitivity, ROW_HEIGHT));

    add(panel, BorderLayout.NORTH);
  }

  public EMouseAction getMouseWheelBehaviour() {
    return EMouseAction.getEnum(mouseWheelBehaviour.getSelectedIndex());
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
    mouseWheelBehaviour.setSelectedIndex(EMouseAction.getOrdinal(getMouseWheelBehaviour(config)));
    zoomSensitivity.setValue(getZoomSensitivity(config));
    scrollSensitivity.setValue(getScrollSensitivity(config));
  }
}
