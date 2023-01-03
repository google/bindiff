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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.SimilarityConfidenceCellRenderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

public class BarChart2dPanel extends ChartPanel {
  private static final Font TITLE_FONT = new Font("Arial", Font.PLAIN, 16);
  private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 12);

  private final CategoryPlot plot;

  public BarChart2dPanel(final String title, final CategoryDataset dataset) {
    super(
        ChartFactory.createBarChart3D(
            title, "", "Matched Functions", dataset, PlotOrientation.VERTICAL, false, true, false),
        false,
        true,
        false,
        false,
        true);

    getChart().getCategoryPlot().getDomainAxis().setVisible(true);
    getChart().getTitle().setFont(TITLE_FONT);
    getChart().setPadding(new RectangleInsets(5., 0., 0., 5.));

    // Disable scaling for good
    setMinimumDrawHeight(0);
    setMinimumDrawWidth(0);
    setMaximumDrawHeight(32768);
    setMaximumDrawWidth(32768);

    plot = getChart().getCategoryPlot();
    plot.setBackgroundPaint(new Color(245, 245, 245));
    plot.setRangeGridlinePaint(new Color(160, 160, 160));
    plot.setNoDataMessage("(No data to display)");
    plot.getRangeAxis().setLabelFont(LABEL_FONT);

    plot.setRenderer(new ChartBarRenderer(getBarColors()));

    final CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(1.));
  }

  private Paint[] getBarColors() {
    final Paint paint[] = new Paint[11];
    for (int i = 0; i <= 10; i++) {
      paint[i] = SimilarityConfidenceCellRenderer.calcColor(i / 10.0);
    }

    return paint;
  }

  public void fireChartChanged() {
    getChart().fireChartChanged();
  }

  public void setSeriesStrokeWidth(final int series, final float width) {
    final CategoryItemRenderer renderer = plot.getRenderer();
    final BasicStroke stroke = new BasicStroke(width);
    renderer.setSeriesStroke(series, stroke);
  }

  public void setTitle(final String title) {
    getChart().setTitle(title);
  }

  public void setTooltipGenerator(final CategoryToolTipGenerator generator) {
    final CategoryItemRenderer renderer = plot.getRenderer();
    renderer.setBaseToolTipGenerator(generator);
  }
}
