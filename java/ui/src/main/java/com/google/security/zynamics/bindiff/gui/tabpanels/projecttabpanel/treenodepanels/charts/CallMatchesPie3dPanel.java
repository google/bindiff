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

public class CallMatchesPie3dPanel extends JPanel {
  public static final int MATCHED_FUNCTIONS = 0;
  public static final int PRIMRAY_UNMATCHED_CALLS = 1;
  public static final int SECONDARY_UNMATCHED_CALLS = 2;

  private static final String[] PLOTS = {
    "Matched Calls", "Primary unmatched Calls", "Secondary unmatched Calls"
  };

  private int matchedCount;
  private int primaryUnmatchedCount;
  private int secondaryUnmatchedCount;

  private double matchedPercent;
  private double primaryUnmatchedPercent;
  private double secondaryUnmatchedPercent;

  private final Diff diff;

  private final Pie3dPanel piePanel;

  private final DefaultPieDataset dataset = new DefaultPieDataset();

  private final InternalFlowgraphCachedCountsListener changeListener =
      new InternalFlowgraphCachedCountsListener();

  public CallMatchesPie3dPanel(final Diff diff) {
    super(new BorderLayout());
    Preconditions.checkNotNull(diff);

    this.diff = diff;

    piePanel = new Pie3dPanel(getTitle(), dataset, new CustomLabelGenerator());

    piePanel.setSectionColor(PLOTS[MATCHED_FUNCTIONS], Colors.PIE_MATCHED);
    piePanel.setSectionColor(PLOTS[PRIMRAY_UNMATCHED_CALLS], Colors.PIE_PRIMARY_UNMATCHED);
    piePanel.setSectionColor(PLOTS[SECONDARY_UNMATCHED_CALLS], Colors.PIE_SECONDARY_UNMATCHED);

    piePanel.setTooltipGenerator(new CustomTooltipGenerator());

    add(piePanel, BorderLayout.CENTER);

    updateDataset();

    diff.getMetaData().addListener(changeListener);
  }

  private String getTitle() {
    if (Double.isNaN(matchedPercent)) {
      return "Calls";
    }

    return String.format("%s %.1f%s", "Calls", matchedPercent, "%");
  }

  private void updateDataset() {
    final MatchData matches = diff.getMatches();

    matchedCount = matches.getSizeOfMatchedCalls();
    primaryUnmatchedCount = matches.getSizeOfUnmatchedCalls(ESide.PRIMARY);
    secondaryUnmatchedCount = matches.getSizeOfUnmatchedCalls(ESide.SECONDARY);

    final int total = matchedCount + primaryUnmatchedCount + secondaryUnmatchedCount;

    matchedPercent = (double) matchedCount / total * 100.;
    primaryUnmatchedPercent = (double) primaryUnmatchedCount / total * 100.;
    secondaryUnmatchedPercent = (double) secondaryUnmatchedCount / total * 100.;

    dataset.setValue(PLOTS[MATCHED_FUNCTIONS], matchedPercent);
    dataset.setValue(PLOTS[PRIMRAY_UNMATCHED_CALLS], primaryUnmatchedPercent);
    dataset.setValue(PLOTS[SECONDARY_UNMATCHED_CALLS], secondaryUnmatchedPercent);

    piePanel.setTitle(getTitle());

    piePanel.fireChartChanged();
  }

  public void dispose() {
    diff.getMetaData().removeListener(changeListener);
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
      String result = null;
      if (dataset != null) {
        if (key.equals(PLOTS[MATCHED_FUNCTIONS])) {
          result = String.format("%d\n%.1f%s", matchedCount, matchedPercent, "%");
        } else if (key.equals(PLOTS[PRIMRAY_UNMATCHED_CALLS])) {
          result = String.format("%d\n%.1f%s", primaryUnmatchedCount, primaryUnmatchedPercent, "%");
        } else if (key.equals(PLOTS[SECONDARY_UNMATCHED_CALLS])) {
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
      if (dataset != null) {
        if (key.equals(PLOTS[MATCHED_FUNCTIONS])) {
          return String.format(
              "%s %d (%.1f%s)", PLOTS[MATCHED_FUNCTIONS], matchedCount, matchedPercent, "%");
        }
        if (key.equals(PLOTS[PRIMRAY_UNMATCHED_CALLS])) {
          return String.format(
              "%s %d (%.1f%s)",
              PLOTS[PRIMRAY_UNMATCHED_CALLS], primaryUnmatchedCount, primaryUnmatchedPercent, "%");
        }
        if (key.equals(PLOTS[SECONDARY_UNMATCHED_CALLS])) {
          return String.format(
              "%s %d (%.1f%s)",
              PLOTS[SECONDARY_UNMATCHED_CALLS],
              secondaryUnmatchedCount,
              secondaryUnmatchedPercent,
              "%");
        }
      }
      return null;
    }
  }

  private class InternalFlowgraphCachedCountsListener extends CountsChangedListener {
    @Override
    public void callsCountChanged() {
      updateDataset();
    }
  }
}
