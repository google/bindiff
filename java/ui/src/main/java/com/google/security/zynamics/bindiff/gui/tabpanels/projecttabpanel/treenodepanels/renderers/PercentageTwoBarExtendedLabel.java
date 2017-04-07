package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers;

import com.google.security.zynamics.bindiff.resources.Colors;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public class PercentageTwoBarExtendedLabel extends JLabel {
  private final PercentageTwoBarExtendedIcon matchBarIcon;

  public PercentageTwoBarExtendedLabel(
      final PercentageTwoBarExtendedCellData data,
      final Color leftBarColor,
      final Color innerLeftBarColor,
      final Color rightBarColor,
      final int labelHeight) {
    matchBarIcon =
        new PercentageTwoBarExtendedIcon(
            data,
            leftBarColor,
            innerLeftBarColor,
            rightBarColor,
            Colors.GRAY160,
            Colors.GRAY32,
            -2,
            0,
            1,
            labelHeight - 1);

    setIcon(matchBarIcon);

    setBorder(new LineBorder(Color.black));
  }

  @Override
  public void paint(final Graphics g) {
    matchBarIcon.setWidth(getWidth() + 1);

    super.paint(g);
  }

  public void updateData(final int leftValue, final int innerLeftValue, final int rightValue) {
    matchBarIcon.updateData(leftValue, innerLeftValue, rightValue);

    updateUI();
  }
}
