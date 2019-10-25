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

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class MiscPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 4;

  private final JComboBox<String> viewSynchronization = new JComboBox<>();
  private final JComboBox<String> gradientBackground = new JComboBox<>();
  private final JComboBox<String> layoutAnimation = new JComboBox<>();
  private final DoubleLabeledSlider animationSpeed =
      new DoubleLabeledSlider("  Slow  ", "  Fast  ", 1, 10, false, new LineBorder(Color.GRAY));

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public MiscPanel(final String borderTitle, final ESettingsDialogType type) {
    super(new BorderLayout());

    Preconditions.checkNotNull(borderTitle);

    if (type == null || type == ESettingsDialogType.NON_INITIAL) {
      throw new IllegalArgumentException("Dialog type cannot be null or non-initial.");
    }

    dialogType = type;

    settings = null;

    animationSpeed.setInverted(true);

    init(borderTitle);
  }

  public MiscPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());

    Preconditions.checkNotNull(borderTitle);

    if (type == null || type != ESettingsDialogType.NON_INITIAL) {
      throw new IllegalArgumentException("Dialog type cannot be null or not non-initial.");
    }

    dialogType = type;

    this.settings = settings;

    animationSpeed.setInverted(true);

    init(borderTitle);
  }

  private int getAnimationSpeed(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return config.getInitialCallgraphSettings().getAnimationSpeed();
      case INITIAL_FLOWGRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getAnimationSpeed();
      default:
    }

    return settings.getDisplaySettings().getAnimationSpeed();
  }

  private boolean getGradientBackground(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return config.getInitialCallgraphSettings().getGradientBackground();
      case INITIAL_FLOWGRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getGradientBackground();
      default:
    }

    return settings.getDisplaySettings().getGradientBackground();
  }

  private boolean getLayoutAnimation(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return config.getInitialCallgraphSettings().getLayoutAnimation();
      case INITIAL_FLOWGRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getLayoutAnimation();
      default:
    }

    return settings.getLayoutSettings().getAnimateLayout();
  }

  private boolean getViewSynchronization(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return config.getInitialCallgraphSettings().getViewSynchronization();
      case INITIAL_FLOWGRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getViewSynchronization();
      default:
    }

    return settings.getGraphSyncMode() == EGraphSynchronization.SYNC;
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    viewSynchronization.addItem("On");
    viewSynchronization.addItem("Off");
    gradientBackground.addItem("On");
    gradientBackground.addItem("Off");
    layoutAnimation.addItem("On");
    layoutAnimation.addItem("Off");

    setCurrentValues();

    final int rows =
        dialogType != ESettingsDialogType.NON_INITIAL ? NUMBER_OF_ROWS : NUMBER_OF_ROWS - 1;

    final JPanel panel = new JPanel(new GridLayout(rows, 1, 5, 5));
    panel.setBorder(new TitledBorder(borderTitle));

    if (dialogType != ESettingsDialogType.NON_INITIAL) {
      panel.add(
          GuiUtils.createHorizontalNamedComponentPanel(
              "Views synchronization", LABEL_WIDTH, viewSynchronization, ROW_HEIGHT));
    }

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Gradient background", LABEL_WIDTH, gradientBackground, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Layout animation", LABEL_WIDTH, layoutAnimation, ROW_HEIGHT));
    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Animation speed", LABEL_WIDTH, animationSpeed, ROW_HEIGHT));

    add(panel, BorderLayout.NORTH);
  }

  public int getAnimationSpeed() {
    return animationSpeed.getValue();
  }

  public boolean getGradientBackground() {
    return gradientBackground.getSelectedIndex() == 0;
  }

  public boolean getLayoutAnimation() {
    return layoutAnimation.getSelectedIndex() == 0;
  }

  public boolean getViewSynchronization() {
    return viewSynchronization.getSelectedIndex() == 0;
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    viewSynchronization.setSelectedIndex(getViewSynchronization(config) ? 0 : 1);
    gradientBackground.setSelectedIndex(getGradientBackground(config) ? 0 : 1);
    layoutAnimation.setSelectedIndex(getLayoutAnimation(config) ? 0 : 1);
    animationSpeed.setValue(getAnimationSpeed(config));
  }
}
