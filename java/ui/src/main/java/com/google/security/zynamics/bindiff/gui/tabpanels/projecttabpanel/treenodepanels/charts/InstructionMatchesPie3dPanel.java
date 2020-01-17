// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.project.diff.CountsChangedListener;
import com.google.security.zynamics.bindiff.project.diff.Diff;
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

public class InstructionMatchesPie3dPanel extends JPanel {
  private static final int MATCHED_INSTRUCTIONS = 0;
  private static final int PRIMRAY_UNMATCHED_INSTRUCTIONS = 1;
  private static final int SECONDARY_UNMATCHED_INSTRUCTIONS = 2;

  private static final String[] PLOTS = {
    "Matched Instructions", "Primary unmatched Instructions", "Secondary unmatched Instructions"
  };

  private int matchedCount = 0;
  private int primaryUnmatchedCount = 0;
  private int secondaryUnmatchedCount = 0;

  private double matchedPercent = 0.;
  private double primaryUnmatchedPercent = 0.;
  private double secondaryUnmatchedPercent = 0.;

  private final boolean includeUnmatchedFunctions;

  private final Diff diff;

  private final Pie3dPanel piePanel;

  private final DefaultPieDataset dataset = new DefaultPieDataset();

  private final InternalFlowgraphCachedCountsListener changeListener =
      new InternalFlowgraphCachedCountsListener();

  public InstructionMatchesPie3dPanel(final Diff diff, final boolean includeUnmtachedFunctions) {
    super(new BorderLayout());

    Preconditions.checkNotNull(diff);

    this.diff = diff;
    includeUnmatchedFunctions = includeUnmtachedFunctions;

    piePanel = new Pie3dPanel(getTitle(), dataset, new CustomLabelGenerator());

    piePanel.setSectionColor(PLOTS[MATCHED_INSTRUCTIONS], Colors.PIE_MATCHED);
    piePanel.setSectionColor(PLOTS[PRIMRAY_UNMATCHED_INSTRUCTIONS], Colors.PIE_PRIMARY_UNMATCHED);
    piePanel.setSectionColor(
        PLOTS[SECONDARY_UNMATCHED_INSTRUCTIONS], Colors.PIE_SECONDARY_UNMATCHED);

    piePanel.setTooltipGenerator(new CustomTooltipGenerator());

    add(piePanel, BorderLayout.CENTER);

    if (includeUnmatchedFunctions) {
      diff.getMetaData().addListener(changeListener);
      updateDataset();
    }
  }

  private String getTitle() {
    if (Double.isNaN(matchedPercent)) {
      return "Instructions";
    }

    return String.format("%s %.1f%s", "Instructions", matchedPercent, "%");
  }

  private void updateDataset() {
    if (includeUnmatchedFunctions) {
      final MatchData matches = diff.getMatches();

      matchedCount = matches.getSizeOfMatchedInstructions();
      primaryUnmatchedCount = matches.getSizeOfUnmatchedInstructions(ESide.PRIMARY);
      secondaryUnmatchedCount = matches.getSizeOfUnmatchedInstructions(ESide.SECONDARY);

      final int total = matchedCount + primaryUnmatchedCount + secondaryUnmatchedCount;

      matchedPercent = (double) matchedCount / total * 100;
      primaryUnmatchedPercent = (double) primaryUnmatchedCount / total * 100;
      secondaryUnmatchedPercent = (double) secondaryUnmatchedCount / total * 100;

      dataset.setValue(PLOTS[MATCHED_INSTRUCTIONS], matchedPercent);
      dataset.setValue(PLOTS[PRIMRAY_UNMATCHED_INSTRUCTIONS], primaryUnmatchedPercent);
      dataset.setValue(PLOTS[SECONDARY_UNMATCHED_INSTRUCTIONS], secondaryUnmatchedPercent);

      piePanel.setTitle(getTitle());

      piePanel.fireChartChanged();
    }
  }

  public void dispose() {
    if (includeUnmatchedFunctions) {
      diff.getMetaData().removeListener(changeListener);
    }
  }

  public JFreeChart getChart() {
    return piePanel.getChart();
  }

  public void updateDataset(
      final int matchedInstructions,
      final int primaryUnmatchedInstructions,
      final int secondaryUnmatchedInstructions) {
    if (!includeUnmatchedFunctions) {
      matchedCount = matchedInstructions;
      primaryUnmatchedCount = primaryUnmatchedInstructions;
      secondaryUnmatchedCount = secondaryUnmatchedInstructions;

      final int total = matchedCount + primaryUnmatchedCount + secondaryUnmatchedCount;

      matchedPercent = (double) matchedCount / total * 100.;
      primaryUnmatchedPercent = (double) primaryUnmatchedCount / total * 100.;
      secondaryUnmatchedPercent = (double) secondaryUnmatchedCount / total * 100.;

      dataset.setValue(PLOTS[MATCHED_INSTRUCTIONS], matchedPercent);
      dataset.setValue(PLOTS[PRIMRAY_UNMATCHED_INSTRUCTIONS], primaryUnmatchedPercent);
      dataset.setValue(PLOTS[SECONDARY_UNMATCHED_INSTRUCTIONS], secondaryUnmatchedPercent);

      piePanel.setTitle(getTitle());

      piePanel.fireChartChanged();
    }
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
      String result = null;
      if (dataset != null) {
        if (key.equals(PLOTS[MATCHED_INSTRUCTIONS])) {
          result = String.format("%d\n%.1f%s", matchedCount, matchedPercent, "%");
        } else if (key.equals(PLOTS[PRIMRAY_UNMATCHED_INSTRUCTIONS])) {
          result = String.format("%d\n%.1f%s", primaryUnmatchedCount, primaryUnmatchedPercent, "%");
        } else if (key.equals(PLOTS[SECONDARY_UNMATCHED_INSTRUCTIONS])) {
          result =
              String.format("%d\n%.1f%s", secondaryUnmatchedCount, secondaryUnmatchedPercent, "%");
        }
      }
      return result;
    }
  }

  private class CustomTooltipGenerator implements PieToolTipGenerator {
    @Override
    @SuppressWarnings("rawtypes")
    public String generateToolTip(final PieDataset dataset, final Comparable key) {
      String result = null;
      if (dataset != null) {
        if (key.equals(PLOTS[MATCHED_INSTRUCTIONS])) {
          result =
              String.format(
                  "%s %d (%.1f%s)", PLOTS[MATCHED_INSTRUCTIONS], matchedCount, matchedPercent, "%");
        } else if (key.equals(PLOTS[PRIMRAY_UNMATCHED_INSTRUCTIONS])) {
          result =
              String.format(
                  "%s %d (%.1f%s)",
                  PLOTS[PRIMRAY_UNMATCHED_INSTRUCTIONS],
                  primaryUnmatchedCount,
                  primaryUnmatchedPercent,
                  "%");
        } else if (key.equals(PLOTS[SECONDARY_UNMATCHED_INSTRUCTIONS])) {
          result =
              String.format(
                  "%s %d (%.1f%s)",
                  PLOTS[SECONDARY_UNMATCHED_INSTRUCTIONS],
                  secondaryUnmatchedCount,
                  secondaryUnmatchedPercent,
                  "%");
        }
      }
      return result;
    }
  }

  private class InternalFlowgraphCachedCountsListener extends CountsChangedListener {
    @Override
    public void instructionsCountsChanged() {
      updateDataset();
    }
  }
}
