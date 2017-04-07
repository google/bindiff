package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts;

import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Vector;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class SimilarityBarChart2dPanel extends JPanel {
  private static final int SIMILARITY = 0;

  private static final String SERIES = "Similarity";

  private final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

  private final BarChart2dPanel barChart;

  private final double similarity;

  public SimilarityBarChart2dPanel(final DiffMetaData metadata) {
    super(new BorderLayout());

    similarity = metadata.getTotalSimilarity();
    barChart = new BarChart2dPanel(getTitle(), dataset);

    init();

    updateDataset(metadata.getSimilarityIntervalCounts());

    setPreferredSize(new Dimension(200, 200));

    add(barChart, BorderLayout.CENTER);
  }

  private String getTitle() {
    return similarity == -1 ? SERIES : String.format("%s %.2f", SERIES, similarity);
  }

  private void init() {
    barChart.setSeriesStrokeWidth(SIMILARITY, 2.f);
    barChart.setTooltipGenerator(new CustomTooltipGenerator());
  }

  private void updateDataset(final int[] similarityIntervalCounts) {
    for (int i = 0; i <= 10; i++) {
      dataset.addValue(similarityIntervalCounts[i], SERIES, Double.valueOf(i / 10.0));
    }
  }

  public JFreeChart getChart() {
    return barChart.getChart();
  }

  public void updateDataset(final Vector<Double> similarities) {
    if (similarities.size() == 0) {
      return;
    }

    final int counter[] = new int[11];
    for (final double similarity : similarities) {
      counter[(int) Math.floor(similarity * 10)]++;
    }

    dataset.clear();
    updateDataset(counter);

    barChart.setTitle(getTitle());
    barChart.fireChartChanged();
  }

  public class CustomTooltipGenerator implements CategoryToolTipGenerator {
    @Override
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
      String result = null;

      final double similarity = column / 10.;

      String range = "";
      if (similarity == 1.) {
        range = "= 1.0";
      } else {
        range = String.format("%.1f - %.1f", similarity, similarity + 0.1);
      }
      result =
          String.format(
              "%.0f %s (%s %s)",
              (double) dataset.getValue(row, column), "Matched Functions", "Similarity", range);

      return result;
    }
  }
}
