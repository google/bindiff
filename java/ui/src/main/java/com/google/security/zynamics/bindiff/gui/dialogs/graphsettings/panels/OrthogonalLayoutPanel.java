package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.enums.ELayoutOrientation;
import com.google.security.zynamics.bindiff.enums.EOrthogonalLayoutStyle;
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

public class OrthogonalLayoutPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 3;

  private final JComboBox<String> orientation = new JComboBox<>();
  private final JComboBox<String> layoutStyle = new JComboBox<>();
  private final JFormattedTextField minimumNodeDistance =
      new JFormattedTextField(new DefaultFormatterFactory(new CDecFormatter(3)));

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public OrthogonalLayoutPanel(final String borderTitle, final ESettingsDialogType type) {
    super(new BorderLayout());

    Preconditions.checkNotNull(borderTitle);

    if (type == null || type == ESettingsDialogType.NON_INITIAL) {
      throw new IllegalArgumentException("Dialog type cannot be null or non-initial.");
    }

    dialogType = type;

    settings = null;

    init(borderTitle);
  }

  public OrthogonalLayoutPanel(
      final String borderTitle, final ESettingsDialogType type, final GraphSettings settings) {
    super(new BorderLayout());

    Preconditions.checkNotNull(borderTitle);

    if (type == null || type != ESettingsDialogType.NON_INITIAL) {
      throw new IllegalArgumentException("Dialog type cannot be null or not non-initial.");
    }

    dialogType = type;

    this.settings = settings;

    init(borderTitle);
  }

  private int getMinimumNodeDistance(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return config.getInitialCallgraphSettings().getOrthogonalMinimumNodeDistance();
      case INITIAL_FLOWGRAPH_SETTINGS:
        return config.getInitialFlowGraphSettings().getOrthogonalMinimumNodeDistance();
      default:
    }

    return (int) settings.getLayoutSettings().getMinimumOrthogonalNodeDistance();
  }

  private EOrthogonalLayoutStyle getOrthogonalLayoutStyle(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return EOrthogonalLayoutStyle.getEnum(
            config.getInitialCallgraphSettings().getOrthogonalLayoutStyle());
      case INITIAL_FLOWGRAPH_SETTINGS:
        return EOrthogonalLayoutStyle.getEnum(
            config.getInitialFlowGraphSettings().getOrthogonalLayoutStyle());
      default:
    }

    return settings.getLayoutSettings().getOrthogonalLayoutStyle();
  }

  private ELayoutOrientation getOrthogonalOrientation(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return ELayoutOrientation.getEnum(
            config.getInitialCallgraphSettings().getOrthogonalOrientation());
      case INITIAL_FLOWGRAPH_SETTINGS:
        return ELayoutOrientation.getEnum(
            config.getInitialFlowGraphSettings().getOrthogonalOrientation());
      default:
    }

    return settings.getLayoutSettings().getOrthogonalLayoutOrientation();
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    layoutStyle.addItem("Normal");
    layoutStyle.addItem("Tree");
    orientation.addItem("Horizontal");
    orientation.addItem("Vertical");

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
    return EOrthogonalLayoutStyle.getEnum(layoutStyle.getSelectedIndex());
  }

  public ELayoutOrientation getOrthogonalOrientation() {
    return ELayoutOrientation.getEnum(orientation.getSelectedIndex());
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    layoutStyle.setSelectedIndex(
        EOrthogonalLayoutStyle.getOrdinal(getOrthogonalLayoutStyle(config)));
    orientation.setSelectedIndex(ELayoutOrientation.getOrdinal(getOrthogonalOrientation(config)));
    minimumNodeDistance.setText(Integer.toString(getMinimumNodeDistance(config)));
  }
}
