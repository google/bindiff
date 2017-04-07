package com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.panels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.config.BinDiffConfig;
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

public class EdgesPanel extends JPanel {
  private static final int LABEL_WIDTH = 275;
  private static final int ROW_HEIGHT = 25;
  private static final int NUMBER_OF_ROWS = 1;

  private final JComboBox<String> drawBends = new JComboBox<>();

  private final ESettingsDialogType dialogType;

  private final GraphSettings settings;

  public EdgesPanel(final String borderTitle, final ESettingsDialogType type) {
    super(new BorderLayout());
    Preconditions.checkNotNull(borderTitle);

    if (type == null || type == ESettingsDialogType.NON_INITIAL) {
      throw new IllegalArgumentException("Dialog type cannot be null or non-initial.");
    }

    dialogType = type;

    settings = null;

    init(borderTitle);
  }

  public EdgesPanel(
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

  private boolean getDrawBends(final BinDiffConfig config) {
    switch (dialogType) {
      case INITIAL_CALLGRAPH_SETTING:
        return config.getInitialCallgraphSettings().getDrawBends();
      case INITIAL_FLOWGRAPH_SETTINGS:
        return config.getInitialFlowgraphSettings().getDrawBends();
      default:
    }

    return settings.getDrawBends();
  }

  private void init(final String borderTitle) {
    setBorder(new LineBorder(Color.GRAY));

    drawBends.addItem("On");
    drawBends.addItem("Off");

    setCurrentValues();

    final JPanel panel = new JPanel(new GridLayout(NUMBER_OF_ROWS, 1, 5, 5));
    panel.setBorder(new TitledBorder(borderTitle));

    panel.add(
        GuiUtils.createHorizontalNamedComponentPanel(
            "Draw bends", LABEL_WIDTH, drawBends, ROW_HEIGHT));

    add(panel, BorderLayout.NORTH);
  }

  public boolean getDrawBends() {
    return drawBends.getSelectedIndex() == 0;
  }

  public void setCurrentValues() {
    final BinDiffConfig config = BinDiffConfig.getInstance();

    drawBends.setSelectedIndex(getDrawBends(config) ? 0 : 1);
  }
}
