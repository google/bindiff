// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SliderPanel extends JPanel {
  private final JSlider slider;

  private final JLabel value;

  private final int maxVal;

  private final int minVal;

  private final boolean showInfinityOnMax;

  private final ChangeListener sliderListener = new InternalSliderListener();

  public SliderPanel(
      final int val,
      final int min,
      final int max,
      final boolean infinityOnMax,
      final boolean paintTicks,
      final boolean paintTrack,
      final boolean paintBorder,
      final int labelWidth,
      final int panelHeight) {
    super(new BorderLayout());

    validateValue(val);

    maxVal = max;
    minVal = min;

    showInfinityOnMax = infinityOnMax;

    slider = new JSlider(min, max);
    slider.setValue(val);

    slider.setMinorTickSpacing(1);
    slider.setPaintTicks(paintTicks);
    slider.setPaintTrack(paintTrack);
    slider.setPaintLabels(false);

    slider.addChangeListener(sliderListener);

    value = new JLabel();
    value.setText(Integer.toString(val));
    value.setBorder(new EmptyBorder(0, 2, 0, 0));

    if (paintBorder) {
      setBorder(new LineBorder(Color.GRAY));
    }

    add(value, BorderLayout.WEST);
    add(slider, BorderLayout.CENTER);

    value.setPreferredSize(new Dimension(labelWidth, panelHeight));
  }

  private void validateValue(final int val) {
    if (val > maxVal || val < minVal) {
      throw new IllegalArgumentException("Value is out of range.");
    }
  }

  public void dispose() {
    slider.removeChangeListener(sliderListener);
  }

  public int getValue() {
    return slider.getValue();
  }

  public void setValue(final int val) {
    validateValue(val);

    slider.setValue(val);
  }

  public void setValueText(final String text) {
    value.setText(text);
  }

  private class InternalSliderListener implements ChangeListener {
    @Override
    public void stateChanged(final ChangeEvent event) {
      setValueText(
          slider.getValue() == maxVal && showInfinityOnMax
              ? "<html>&infin;</html>"
              : Integer.toString(slider.getValue()));
    }
  }
}
