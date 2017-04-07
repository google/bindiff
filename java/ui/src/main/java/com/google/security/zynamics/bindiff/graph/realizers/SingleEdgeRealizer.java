package com.google.security.zynamics.bindiff.graph.realizers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IEdgeRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.view.BendCursor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

public class SingleEdgeRealizer extends ZyEdgeRealizer<SingleDiffEdge> {
  public static final Color BEND_SELECTION_COLOR = new Color(255, 200, 0);

  private final GraphSettings settings;

  public SingleEdgeRealizer(
      final ZyLabelContent content,
      final IEdgeRealizerUpdater<SingleDiffEdge> updater,
      final GraphSettings settings) {
    super(content, updater);

    Preconditions.checkNotNull(settings);

    this.settings = settings;

    setSmoothedBends(true);
  }

  @Override
  public void paintBends(final Graphics2D gfx) {
    if (settings.getDrawBends()) {
      for (final BendCursor bc = bends(); bc.ok(); bc.next()) {
        final double x = bc.bend().getX();
        final double y = bc.bend().getY();

        final boolean selected = bc.bend().isSelected();

        final Color oldColor = gfx.getColor();
        final Stroke oldSroke = gfx.getStroke();

        final float strokeWidth = selected ? 2.f : 1.f;
        final int diameter = selected ? 7 : 5;
        final float offset = selected ? 3 : 2;

        gfx.setStroke(new BasicStroke(strokeWidth));
        gfx.setColor(selected ? BEND_SELECTION_COLOR : Color.WHITE);

        gfx.fill(new Ellipse2D.Double(x - offset, y - offset, diameter, diameter));

        gfx.setColor(Color.BLACK);
        gfx.draw(new Ellipse2D.Double(x - offset, y - offset, diameter, diameter));

        gfx.setColor(oldColor);
        gfx.setStroke(oldSroke);
      }
    }
  }
}
