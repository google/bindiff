package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers;

import com.google.security.zynamics.bindiff.resources.Colors;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public class PercentageTwoBarLabel extends JLabel {
  private final PercentageTwoBarIcon matchBarIcon;

  public PercentageTwoBarLabel(
      final PercentageTwoBarCellData data,
      final Color leftBarColor,
      final Color rightBarColor,
      final int labelHeight) {
    matchBarIcon =
        new PercentageTwoBarIcon(
            data,
            leftBarColor,
            rightBarColor,
            Colors.GRAY160,
            Colors.GRAY32,
            Colors.GRAY32,
            Colors.GRAY32,
            Colors.GRAY32,
            false,
            -2,
            0,
            1,
            labelHeight - 1);

    matchBarIcon.showAdditionalPercetageValues(true);
    setIcon(matchBarIcon);
    setBorder(new LineBorder(Color.black));
  }

  @Override
  public void paint(final Graphics g) {
    matchBarIcon.setWidth(getWidth() + 1);

    super.paint(g);
  }

  public void updateData(final int leftValue, final int rightValue) {
    matchBarIcon.updateData(leftValue, rightValue);

    updateUI();
  }
}
