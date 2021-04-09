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
import com.google.security.zynamics.bindiff.resources.Fonts;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
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
  public void paint(final Graphics2D g2, final int x, final int y, final int w, final int h) {
    if (g2 == null) {
      return;
    }

    super.paint(g2, x, y, w, h);
    undoWorldTransform(g2);

    g2.setPaint(Color.GRAY.darker());

    final int vw = graphView.getWidth();
    final int ix = getX(vw, secondaryTextImage.getWidth(null));
    int tx = ix;

    final Font font = Fonts.BOLD_FONT;
    g2.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (type == EGraph.SECONDARY_GRAPH) {
      final Rectangle2D bounds = font.getStringBounds(title, g2.getFontRenderContext());
      tx = getX(vw, Math.max(OFFSET, (int) bounds.getWidth()));
    }

    g2.drawString(title, tx, 15);
    g2.drawImage(getTextImage(), ix, 20, null);
  }

  public void update() {
    EventQueue.invokeLater(() -> graphView.getGraph2D().updateViews());
  }
}
