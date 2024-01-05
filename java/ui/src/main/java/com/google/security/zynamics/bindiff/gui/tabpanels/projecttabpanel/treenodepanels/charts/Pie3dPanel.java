// Copyright 2011-2024 Google LLC
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.Rotation;

public class Pie3dPanel extends ChartPanel {
  private static final Font TITLE_FONT = new Font("Arial", Font.PLAIN, 16);
  private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 10);

  private final PiePlot3D section;

  public Pie3dPanel(
      final String title,
      final DefaultPieDataset dataset,
      final PieSectionLabelGenerator customLabelGenerator) {
    super(
        ChartFactory.createPieChart3D(title, dataset, false, true, false),
        false,
        true,
        false,
        false,
        true);

    getChart().setBorderPaint(Color.WHITE);
    getChart().getTitle().setFont(TITLE_FONT);
    getChart().setPadding(new RectangleInsets(5., 0., 0., 0.));

    // Disable scaling for good
    setMinimumDrawHeight(0);
    setMinimumDrawWidth(0);
    setMaximumDrawHeight(32768);
    setMaximumDrawWidth(32768);

    section = (PiePlot3D) getChart().getPlot();
    section.setLabelFont(LABEL_FONT);
    section.setBackgroundPaint(Color.WHITE);
    section.setOutlinePaint(Color.WHITE);
    //    section.setOutlineVisible(false);
    section.setBaseSectionOutlinePaint(Color.WHITE);
    section.setStartAngle(0);
    section.setDirection(Rotation.CLOCKWISE);
    section.setForegroundAlpha(0.5f);
    section.setNoDataMessage("(No data to display)");
    section.setCircular(true);
    section.setLabelGenerator(customLabelGenerator);

    setPreferredSize(new Dimension(240, 200));
  }

  public void fireChartChanged() {
    getChart().fireChartChanged();
  }

  public void setSectionColor(final String plotKey, final Color color) {
    section.setSectionPaint(plotKey, color);
  }

  public void setTitle(final String title) {
    getChart().setTitle(title);
  }

  public void setTooltipGenerator(final PieToolTipGenerator generator) {
    section.setToolTipGenerator(generator);
  }
}
