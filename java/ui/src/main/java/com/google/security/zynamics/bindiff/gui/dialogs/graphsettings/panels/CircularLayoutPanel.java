package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.enums.ECircularLayoutStyle;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
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

public class CircularLayoutPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 2;

  private final JComboBox<String> layoutStyle = new JComboBox<>();
  private final JFormattedTextField minimumNodeDistance =
      new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(3)));

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public CircularLayoutPanel(final String borderTitle, final ESettingsDialogType type) {
    this(borderTitle, type, null);
  }

  public CircularLayoutPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());

    Preconditions.checkNotNull(borderTitle);
    Preconditions.checkArgument(settings == null ^ type == ESettingsDialogType.GRAPH_VIEW_SETTINGS);

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
    return ECircularLayoutStyle.getEnum(layoutStyle.getSelectedIndex());
  }

  public int getMinimumNodeDistance() {
    return Integer.parseInt(minimumNodeDistance.getText());
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    layoutStyle.setSelectedIndex(ECircularLayoutStyle.getOrdinal(getCircularLayoutStyle(config)));

    minimumNodeDistance.setText(Integer.toString(getMinimumNodeDistance(config)));
  }
}
