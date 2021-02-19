// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
