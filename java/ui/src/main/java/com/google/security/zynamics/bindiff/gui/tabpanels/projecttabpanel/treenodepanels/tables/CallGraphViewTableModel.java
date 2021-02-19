// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators.PercentageThreeBarCellDataComparator;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.DoubleComparator;
import com.google.security.zynamics.zylib.general.comparators.LexicalComparator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CallGraphViewTableModel extends AbstractTableModel {
  public static final int SIMILARITY = 0;
  public static final int CONFIDENCE = 1;
  public static final int PRIMARY_NAME = 2;
  public static final int SECONDARY_NAME = 3;
  public static final int FUNCTIONS = 4;
  public static final int CALLS = 5;

  private static final String[] COLUMNS = {
    "Similarity", "Confidence", "Primary Name", "Secondary Name", "Functions", "Calls",
  };

  private final List<Pair<Integer, Comparator<?>>> sorters = new ArrayList<>();

  public CallGraphViewTableModel(final Diff diff) {
    super(diff);

    initSorters();
  }

  private void initSorters() {
    sorters.add(new Pair<>(SIMILARITY, new DoubleComparator()));
    sorters.add(new Pair<>(CONFIDENCE, new DoubleComparator()));
    sorters.add(new Pair<>(PRIMARY_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(SECONDARY_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(FUNCTIONS, new PercentageThreeBarCellDataComparator()));
    sorters.add(new Pair<>(CALLS, new PercentageThreeBarCellDataComparator()));
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  @Override
  public String getColumnName(final int index) {
    return COLUMNS[index];
  }

  @Override
  public int getRowCount() {
    return 1;
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    return sorters;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final MatchData matches = getDiff().getMatches();

    final PercentageThreeBarCellData functions =
        new PercentageThreeBarCellData(
            matches.getSizeOfUnmatchedFunctions(ESide.PRIMARY),
            matches.getSizeOfMatchedFunctions(),
            matches.getSizeOfUnmatchedFunctions(ESide.SECONDARY));

    final PercentageThreeBarCellData calls =
        new PercentageThreeBarCellData(
            matches.getSizeOfUnmatchedCalls(ESide.PRIMARY),
            matches.getSizeOfMatchedCalls(),
            matches.getSizeOfUnmatchedCalls(ESide.SECONDARY));

    final DiffMetadata metaData = getDiff().getMetadata();

    switch (col) {
      case SIMILARITY:
        return metaData.getTotalSimilarity();
      case CONFIDENCE:
        return metaData.getTotalConfidence();
      case PRIMARY_NAME:
        return metaData.getImageName(ESide.PRIMARY);
      case SECONDARY_NAME:
        return metaData.getImageName(ESide.SECONDARY);
      case FUNCTIONS:
        return functions;
      case CALLS:
        return calls;
      default: // fall out
    }

    return null;
  }
}
