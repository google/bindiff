package com.google.security.zynamics.bindiff.graph.backgroundrendering;

import com.google.security.zynamics.bindiff.enums.EGraph;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.bindiff.utils.ImageUtils;

import y.view.DefaultBackgroundRenderer;
import y.view.Graph2DView;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.ScrollPaneConstants;

public class ImageBackgroundRenderer extends DefaultBackgroundRenderer {
  private static final int OFFSET = 15;

  private final Image PRIMARY_TEXT_IMAGE = ImageUtils.getImage("data/graphview/primary.png");
  private final Image SECONDRAY_TEXT_IMAGE = ImageUtils.getImage("data/graphview/secondary.png");
  private final Image COMBINED_TEXT_IMAGE = ImageUtils.getImage("data/graphview/combined.png");

  private final Graph2DView graphView;
  private final EGraph type;
  private int oldViewWidth = 0;
  private int oldViewHeight = 0;
  private BufferedImage backgroundImage;
  private final String title;

  public ImageBackgroundRenderer(
      final ViewData viewData, final Graph2DView view, final EGraph type) {
    super(view);
    this.graphView = view;
    this.type = type;

    title = BackgroundRendererManager.buildTitle(viewData, type);
  }

  private Image getTextImage() {
    if (type == EGraph.PRIMARY_GRAPH) {
      return PRIMARY_TEXT_IMAGE;
    } else if (type == EGraph.SECONDARY_GRAPH) {
      return SECONDRAY_TEXT_IMAGE;
    }

    return COMBINED_TEXT_IMAGE;
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
      final Graphics2D imageGfx = (Graphics2D) backgroundImage.getGraphics();
      imageGfx.setPaint(Color.WHITE);
      imageGfx.fill(new Rectangle2D.Double(0, 0, vw, vh));

      imageGfx.setPaint(Color.GRAY.darker());

      final int ix = getX(vw, SECONDRAY_TEXT_IMAGE.getWidth(null));
      int tx = ix;

      final Font font = imageGfx.getFont();
      final Font newFont = new Font(font.getName(), Font.BOLD, font.getSize());
      imageGfx.setFont(newFont);
      imageGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (type == EGraph.SECONDARY_GRAPH) {
        final Rectangle2D bounds =
            imageGfx.getFont().getStringBounds(title, imageGfx.getFontRenderContext());

        tx = getX(vw, Math.max(OFFSET, (int) bounds.getWidth()));
      }

      imageGfx.drawString(title, tx, 15);
      imageGfx.drawImage(getTextImage(), ix, 20, null);
      imageGfx.setFont(font);

      setImage(backgroundImage);
    }

    super.paint(gfx, x, y, w, h);
  }

  public void update() {
    EventQueue.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            graphView.getGraph2D().updateViews();
          }
        });
  }
}
