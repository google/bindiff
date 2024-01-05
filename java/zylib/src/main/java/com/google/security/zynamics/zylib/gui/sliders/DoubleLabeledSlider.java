// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

  public DoubleLabeledSlider(
      final String leftText, final String rightText, final int min, final int max) {
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

  public DoubleLabeledSlider(
      final String leftText,
      final String rightText,
      final int min,
      final int max,
      final boolean trackbar,
      final Border border) {
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
