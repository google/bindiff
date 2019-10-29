package com.google.security.zynamics.zylib.gui.sliders;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;

public class DoubleLabeledSlider extends JPanel {
  private final JLabel leftLabel;
  private final JSlider slider;
  private final JLabel rightLabel;

  public DoubleLabeledSlider(final String leftText, final String rightText, final int min,
      final int max) {
    setLayout(new BorderLayout());

    leftLabel = new JLabel(leftText);

    slider = new JSlider(min, max);
    slider.setMinorTickSpacing(1);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);

    rightLabel = new JLabel(rightText);

    add(leftLabel, BorderLayout.WEST);
    add(slider);
    add(rightLabel, BorderLayout.EAST);
  }

  public DoubleLabeledSlider(final String leftText, final String rightText, final int min,
      final int max, final boolean trackbar, final Border border) {
    this(leftText, rightText, min, max);

    slider.setPaintTrack(trackbar);

    setBorder(border);
  }

  public int getValue() {
    return slider.getValue();
  }

  public void setInverted(final boolean inverted) {
    slider.setInverted(inverted);
  }

  public void setValue(final int value) {
    slider.setValue(value);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    leftLabel.setEnabled(enabled);
    slider.setEnabled(enabled);
    rightLabel.setEnabled(enabled);
  }
}
