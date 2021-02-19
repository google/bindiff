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

package com.google.security.zynamics.bindiff.graph.backgroundrendering;

import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.ScrollPaneConstants;
import y.view.DefaultBackgroundRenderer;
import y.view.Graph2DView;

/** Renders the graph view background with indicators for the type of graph being shown. */
public class ImageBackgroundRenderer extends DefaultBackgroundRenderer {
  private static final int OFFSET = 15;

  private final Image primaryTextImage = ResourceUtils.getImage("data/graphview/primary.png");
  private final Image secondaryTextImage = ResourceUtils.getImage("data/graphview/secondary.png");
  private final Image combinedTextImage = ResourceUtils.getImage("data/graphview/combined.png");

  private final Graph2DView graphView;
  private final EGraph type;
  private int oldViewWidth = 0;
  private int oldViewHeight = 0;
  private BufferedImage backgroundImage;
  private final String title;

  ImageBackgroundRenderer(final ViewData viewData, final Graph2DView view, final EGraph type) {
    super(view);
    this.graphView = view;
    this.type = type;

    title = BackgroundRendererManager.buildTitle(viewData, type);
  }

  private Image getTextImage() {
    if (type == EGraph.PRIMARY_GRAPH) {
      return primaryTextImage;
    }
    if (type == EGraph.SECONDARY_GRAPH) {
      return secondaryTextImage;
    }
    return combinedTextImage;
  }

  private int getX(final int viewWidth, final int objectWidth) {
    if (type == EGraph.SECONDARY_GRAPH) {
      int offset = OFFSET;

      if (graphView.getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS) {
        offset += 15;
      }

      return viewWidth - objectWidth - offset;
    }

    return OFFSET;
  }

  @Override
  public void paint(final Graphics2D gfx, final int x, final int y, final int w, final int h) {
    if (gfx == null) {
      return;
    }

    final int vw = graphView.getWidth();
    final int vh = graphView.getHeight();

    if (vw != oldViewWidth || vh != oldViewHeight && vw != 0 && vh != 0) {
      oldViewWidth = vw;
      oldViewHeight = vh;

      backgroundImage = new BufferedImage(vw, vh, BufferedImage.TYPE_INT_RGB);
    }

    if (backgroundImage != null) {
      final Graphics2D g = (Graphics2D) backgroundImage.getGraphics();
      g.setPaint(Color.WHITE);
      g.fill(new Rectangle2D.Double(0, 0, vw, vh));

      g.setPaint(Color.GRAY.darker());

      final int ix = getX(vw, secondaryTextImage.getWidth(null));
      int tx = ix;

      final Font font = g.getFont();
      g.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (type == EGraph.SECONDARY_GRAPH) {
        final Rectangle2D bounds =
            g.getFont().getStringBounds(title, g.getFontRenderContext());
        tx = getX(vw, Math.max(OFFSET, (int) bounds.getWidth()));
      }

      g.drawString(title, tx, 15);
      g.drawImage(getTextImage(), ix, 20, null);
      g.setFont(font);

      setImage(backgroundImage);
    }

    super.paint(gfx, x, y, w, h);
  }

  public void update() {
    EventQueue.invokeLater(() -> graphView.getGraph2D().updateViews());
  }
}
