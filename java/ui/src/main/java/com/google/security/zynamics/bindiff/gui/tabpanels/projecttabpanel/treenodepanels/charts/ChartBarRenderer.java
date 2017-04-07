package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts;

import java.awt.Paint;
import org.jfree.chart.renderer.category.BarRenderer3D;

public class ChartBarRenderer extends BarRenderer3D {
  private final Paint[] colors;

  public ChartBarRenderer(final Paint[] colors) {
    this.colors = colors;
  }

  @Override
  public Paint getItemPaint(final int row, final int column) {
    return this.colors[column % this.colors.length];
  }
}
