package com.google.security.zynamics.bindiff.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public abstract class TabPanel extends JPanel {
  public TabPanel() {
    super(new BorderLayout());

    setBorder(new LineBorder(Color.GRAY));
  }

  public abstract Icon getIcon();

  public abstract JMenuBar getMenuBar();

  public abstract String getTitle();
}
