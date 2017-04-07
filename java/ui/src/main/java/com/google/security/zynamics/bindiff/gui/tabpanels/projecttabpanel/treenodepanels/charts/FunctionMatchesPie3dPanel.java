package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.resources.Colors;
import java.awt.BorderLayout;
import java.text.AttributedString;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public class FunctionMatchesPie3dPanel extends JPanel {
  private static final String MATCHED_FUNCTIONS = "Matched Functions";
  private static final String PRIMRAY_UNMATCHED_FUNCTIONS = "Primary unmatched Functions";
  private static final String SECONDARY_UNMATCHED_FUNCTIONS = "Secondary unmatched Functions";

  private final Pie3dPanel piePanel;

  private final int matchedCount;
  private final int primaryUnmatchedCount;
  private final int secondaryUnmatchedCount;

  private final double matchedPercent;
  private final double primaryUnmatchedPercent;
  private final double secondaryUnmatchedPercent;

  public FunctionMatchesPie3dPanel(final Diff diff) {
    super(new BorderLayout());

    Preconditions.checkNotNull(diff);

    final MatchData matches = diff.getMatches();

    matchedCount = matches.getSizeOfMatchedFunctions();
    primaryUnmatchedCount = matches.getSizeOfUnmatchedFunctions(ESide.PRIMARY);
    secondaryUnmatchedCount = matches.getSizeOfUnmatchedFunctions(ESide.SECONDARY);

    final int total = matchedCount + primaryUnmatchedCount + secondaryUnmatchedCount;

    matchedPercent = (double) matchedCount / total * 100.0;
    primaryUnmatchedPercent = (double) primaryUnmatchedCount / total * 100.0;
    secondaryUnmatchedPercent = (double) secondaryUnmatchedCount / total * 100.0;

    final DefaultPieDataset dataset = new DefaultPieDataset();
    dataset.setValue(MATCHED_FUNCTIONS, matchedPercent);
    dataset.setValue(PRIMRAY_UNMATCHED_FUNCTIONS, primaryUnmatchedPercent);
    dataset.setValue(SECONDARY_UNMATCHED_FUNCTIONS, secondaryUnmatchedPercent);

    piePanel = new Pie3dPanel(getTitle(), dataset, new CustomLabelGenerator());

    piePanel.setSectionColor(MATCHED_FUNCTIONS, Colors.PIE_MATCHED);
    piePanel.setSectionColor(PRIMRAY_UNMATCHED_FUNCTIONS, Colors.PIE_PRIMARY_UNMATCHED);
    piePanel.setSectionColor(SECONDARY_UNMATCHED_FUNCTIONS, Colors.PIE_SECONDARY_UNMATCHED);

    piePanel.setTooltipGenerator(new CustomTooltipGenerator());

    add(piePanel, BorderLayout.CENTER);
  }

  private String getTitle() {
    if (Double.isNaN(matchedPercent)) {
      return "Functions";
    }

    return String.format("%s %.1f%s", "Functions", matchedPercent, "%");
  }

  public FunctionMatchesPie3dPanel(final DiffMetaData metadata) {
    super(new BorderLayout());

    Preconditions.checkNotNull(metadata);

    matchedCount = metadata.getSizeOfMatchedFunctions();
    primaryUnmatchedCount = metadata.getSizeOfUnmatchedFunctions(ESide.PRIMARY);
    secondaryUnmatchedCount = metadata.getSizeOfUnmatchedFunctions(ESide.SECONDARY);

    final int total = matchedCount + primaryUnmatchedCount + secondaryUnmatchedCount;

    matchedPercent = (double) matchedCount / total * 100.0;
    primaryUnmatchedPercent = (double) primaryUnmatchedCount / total * 100.0;
    secondaryUnmatchedPercent = (double) secondaryUnmatchedCount / total * 100.0;

    final DefaultPieDataset dataset = new DefaultPieDataset();
    dataset.setValue(MATCHED_FUNCTIONS, matchedPercent);
    dataset.setValue(PRIMRAY_UNMATCHED_FUNCTIONS, primaryUnmatchedPercent);
    dataset.setValue(SECONDARY_UNMATCHED_FUNCTIONS, secondaryUnmatchedPercent);

    piePanel =
        new Pie3dPanel(
            String.format("%s %.1f%s", "Functions", matchedPercent, "%"),
            dataset,
            new CustomLabelGenerator());

    piePanel.setSectionColor(MATCHED_FUNCTIONS, Colors.PIE_MATCHED);
    piePanel.setSectionColor(PRIMRAY_UNMATCHED_FUNCTIONS, Colors.PIE_PRIMARY_UNMATCHED);
    piePanel.setSectionColor(SECONDARY_UNMATCHED_FUNCTIONS, Colors.PIE_SECONDARY_UNMATCHED);

    piePanel.setTooltipGenerator(new CustomTooltipGenerator());

    add(piePanel, BorderLayout.CENTER);
  }

  public JFreeChart getChart() {
    return piePanel.getChart();
  }

  private class CustomLabelGenerator implements PieSectionLabelGenerator {
    @Override
    @SuppressWarnings("rawtypes")
    public AttributedString generateAttributedSectionLabel(
        final PieDataset arg0, final Comparable arg1) {
      return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String generateSectionLabel(final PieDataset dataset, final Comparable key) {
      if (dataset == null) {
        return null;
      }
      if (key.equals(MATCHED_FUNCTIONS)) {
        return String.format("%d\n%.1f%s", matchedCount, matchedPercent, "%");
      }
      if (key.equals(PRIMRAY_UNMATCHED_FUNCTIONS)) {
        return String.format("%d\n%.1f%s", primaryUnmatchedCount, primaryUnmatchedPercent, "%");
      }
      if (key.equals(SECONDARY_UNMATCHED_FUNCTIONS)) {
        return String.format("%d\n%.1f%s", secondaryUnmatchedCount, secondaryUnmatchedPercent, "%");
      }
      return null;
    }
  }

  private class CustomTooltipGenerator implements PieToolTipGenerator {
    @Override
    @SuppressWarnings("rawtypes")
    public String generateToolTip(final PieDataset dataset, final Comparable key) {
      if (dataset == null) {
        return null;
      }
      if (key.equals(MATCHED_FUNCTIONS)) {
        return String.format(
            "%s %d (%.1f%s)", MATCHED_FUNCTIONS, matchedCount, matchedPercent, "%");
      }
      if (key.equals(PRIMRAY_UNMATCHED_FUNCTIONS)) {
        return String.format(
            "%s %d (%.1f%s)",
            PRIMRAY_UNMATCHED_FUNCTIONS, primaryUnmatchedCount, primaryUnmatchedPercent, "%");
      }
      if (key.equals(SECONDARY_UNMATCHED_FUNCTIONS)) {
        return String.format(
            "%s %d (%.1f%s)",
            SECONDARY_UNMATCHED_FUNCTIONS, secondaryUnmatchedCount, secondaryUnmatchedPercent, "%");
      }
      return null;
    }
  }
}
