// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

import y.layout.LabelLayoutConstants;
import y.view.EdgeLabel;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Edge label class that is used to display edge comments.
 */
public class ZyEdgeLabel extends EdgeLabel {
  private static final int RECT_ARC_SIZE = 5;

  private static Color m_backGroundColor = new Color(250, 250, 255);
  private static Color borderColor = Color.BLACK;

  private final ZyLabelContent m_content;

  private final int m_roundedHeight;
  private final int m_roundedWidth;

  private final double m_height;
  private final double m_width;

  public ZyEdgeLabel(final ZyLabelContent content) {
    Preconditions.checkNotNull(content, "Internal Error: Content can't be null");

    m_content = content;

    setVisible(true);
    setText("A"); // Dummy; don't remove

    setModel(EdgeLabel.CENTERED);
    setPreferredPlacement(LabelLayoutConstants.PLACE_ON_EDGE);
    setPosition(EdgeLabel.CENTER);

    m_height = getHeight();
    m_width = getWidth();

    m_roundedHeight = (int) m_height;
    m_roundedWidth = (int) m_width;
  }

  @Override
  protected void paintBox(final Graphics2D gfx, final double x, final double y,
      final double width1, final double height2) {
    final int roundedX = (int) (x - (m_width / 2));
    final int roundedY = (int) (y - (m_height / 2));

    final BasicStroke oldStroke = (BasicStroke) gfx.getStroke();
    gfx.setStroke(new BasicStroke(oldStroke.getLineWidth()));

    gfx.setColor(m_backGroundColor);
    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
    gfx.fillRoundRect(
        roundedX, roundedY, m_roundedWidth, m_roundedHeight, RECT_ARC_SIZE, RECT_ARC_SIZE);

    gfx.setColor(borderColor);
    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
    gfx.drawRoundRect(
        roundedX, roundedY, m_roundedWidth, m_roundedHeight, RECT_ARC_SIZE, RECT_ARC_SIZE);

    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.00f));
    gfx.setStroke(oldStroke);
  }

  @Override
  protected void paintContent(final Graphics2D gfx, final double x, final double y,
      final double width1, final double height1) {
    final int roundedX = (int) (x - (m_width / 2));
    final int roundedY = (int) (y - (m_height / 2));

    m_content.draw(gfx, roundedX, roundedY);
  }

  @Override
  public double getHeight() {
    return m_content.getBounds().getHeight();
  }

  public ZyLabelContent getLabelContent() {
    return m_content;
  }

  @Override
  public double getWidth() {
    return m_content.getBounds().getWidth();
  }
}
