// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.graph.realizers;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IEdgeRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import y.view.BendCursor;

public class CombinedEdgeRealizer extends ZyEdgeRealizer<CombinedDiffEdge> {
  private final GraphSettings settings;

  public CombinedEdgeRealizer(
      final ZyLabelContent content,
      final IEdgeRealizerUpdater<CombinedDiffEdge> updater,
      final GraphSettings settings) {
    super(content, updater);
    this.settings = checkNotNull(settings);
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
        gfx.setColor(selected ? SingleEdgeRealizer.BEND_SELECTION_COLOR : Color.WHITE);

        gfx.fill(new Ellipse2D.Double(x - offset, y - offset, diameter, diameter));

        gfx.setColor(Color.BLACK);
        gfx.draw(new Ellipse2D.Double(x - offset, y - offset, diameter, diameter));

        gfx.setColor(oldColor);
        gfx.setStroke(oldSroke);
      }
    }
  }
}
