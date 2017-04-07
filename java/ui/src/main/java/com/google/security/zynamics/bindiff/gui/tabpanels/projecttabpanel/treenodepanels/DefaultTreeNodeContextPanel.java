package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JLabel;

public class DefaultTreeNodeContextPanel extends AbstractTreeNodeContextPanel {
  private static final Icon IMAGE =
      ImageUtils.getImageIcon(Constants.DEFAULT_BACKGROUND_IMAGE_PATH);

  private final JLabel imageLabel = new JLabel(IMAGE);

  public DefaultTreeNodeContextPanel() {
    init();
  }

  private void init() {
    setMinimumSize(new Dimension(0, 0));
    setBackground(Color.WHITE);

    add(imageLabel);
  }

  @Override
  public List<AbstractTable> getTables() {
    return null;
  }
}
