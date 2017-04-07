// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.sliders;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;

public class DoubleLabeledSlider extends JPanel {
  private static final long serialVersionUID = 3181291967149555468L;

  private final JSlider m_slider;

  public DoubleLabeledSlider(final String leftText, final String rightText, final int min,
      final int max) {
    setLayout(new BorderLayout());

    final JLabel leftLabel = new JLabel(leftText);

    m_slider = new JSlider(min, max);
    m_slider.setMinorTickSpacing(1);
    m_slider.setPaintTicks(true);
    m_slider.setPaintLabels(true);

    final JLabel rightLabel = new JLabel(rightText);

    add(leftLabel, BorderLayout.WEST);
    add(m_slider);
    add(rightLabel, BorderLayout.EAST);
  }

  public DoubleLabeledSlider(final String leftText, final String rightText, final int min,
      final int max, final boolean trackbar, final Border border) {
    this(leftText, rightText, min, max);

    m_slider.setPaintTrack(trackbar);

    setBorder(border);
  }

  public static void main(final String[] args) {
    final JFrame frame = new JFrame();

    final DoubleLabeledSlider slider = new DoubleLabeledSlider("Low", "High", 0, 5);

    frame.add(slider);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.setSize(400, 400);
    frame.setVisible(true);
  }

  public int getValue() {
    return m_slider.getValue();
  }

  public void setInverted(final boolean inverted) {
    m_slider.setInverted(inverted);
  }

  public void setValue(final int value) {
    m_slider.setValue(value);
  }
}
