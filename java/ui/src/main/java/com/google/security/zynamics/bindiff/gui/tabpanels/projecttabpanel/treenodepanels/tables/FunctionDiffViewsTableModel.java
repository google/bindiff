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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators.PercentageThreeBarCellDataComparator;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.helpers.MatchesGetter;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.DoubleComparator;
import com.google.security.zynamics.zylib.general.comparators.HexStringComparator;
import com.google.security.zynamics.zylib.general.comparators.LexicalComparator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FunctionDiffViewsTableModel extends AbstractFunctionDiffViewsTableModel {
  public static final int VIEWNAME = 0;
  public static final int SIMILARITY = 1;
  public static final int CONFIDENCE = 2;
  public static final int PRIMARY_ADDRESS = 3;
  public static final int PRIMARY_NAME = 4;
  public static final int SECONDARY_ADDRESS = 5;
  public static final int SECONDARY_NAME = 6;
  public static final int BASICBLOCK_MATCHES = 7;
  public static final int JUMP_MATCHES = 8;

  private static final String[] COLUMNS = {
    "View Name",
    "Similarity",
    "Confidence",
    "Address",
    "Primary Name",
    "Address",
    "Secondary Name",
    "Basic Blocks",
    "Jumps"
  };

  private final List<Pair<Integer, Comparator<?>>> sorters = new ArrayList<>();

  public FunctionDiffViewsTableModel(final List<Diff> functionDiffViewList) {
    super(functionDiffViewList);
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
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    sorters.add(new Pair<>(VIEWNAME, new LexicalComparator()));
    sorters.add(new Pair<>(PRIMARY_ADDRESS, new HexStringComparator()));
    sorters.add(new Pair<>(PRIMARY_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(BASICBLOCK_MATCHES, new PercentageThreeBarCellDataComparator()));
    sorters.add(new Pair<>(SIMILARITY, new DoubleComparator()));
    sorters.add(new Pair<>(CONFIDENCE, new DoubleComparator()));
    sorters.add(new Pair<>(JUMP_MATCHES, new PercentageThreeBarCellDataComparator()));
    sorters.add(new Pair<>(SECONDARY_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(SECONDARY_ADDRESS, new HexStringComparator()));

    return sorters;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final Diff diff = functionDiffViewList.get(row);

    String viewName = diff.getMatchesDatabase().getName();
    viewName =
        BinDiffFileUtils.forceFilenameEndsNotWithExtension(
            viewName, Constants.BINDIFF_MATCHES_DB_EXTENSION);

    final RawFunction primaryFunction = diff.getCallGraph(ESide.PRIMARY).getNodes().get(0);
    final RawFunction secondaryFunction = diff.getCallGraph(ESide.SECONDARY).getNodes().get(0);

    final IAddress primaryAddr = primaryFunction.getAddress();
    final IAddress secondaryAddr = secondaryFunction.getAddress();

    final int mBbs = primaryFunction.getSizeOfMatchedBasicBlocks();
    final int pUBbs = primaryFunction.getSizeOfBasicBlocks() - mBbs;
    final int sUBbs = secondaryFunction.getSizeOfBasicBlocks() - mBbs;

    final int mJps = primaryFunction.getSizeOfMatchedJumps();
    final int pUJps = primaryFunction.getSizeOfJumps() - mJps;
    final int sUJps = secondaryFunction.getSizeOfJumps() - mJps;

    final PercentageThreeBarCellData basicblocks =
        new PercentageThreeBarCellData(pUBbs, mBbs, sUBbs, getColumnSortRelevance(col));
    final PercentageThreeBarCellData jumps =
        new PercentageThreeBarCellData(pUJps, mJps, sUJps, getColumnSortRelevance(col));

    switch (col) {
      case VIEWNAME:
        return viewName;
      case PRIMARY_ADDRESS:
        return primaryAddr.toHexString();
      case PRIMARY_NAME:
        return primaryFunction.getName();
      case BASICBLOCK_MATCHES:
        return basicblocks;
      case CONFIDENCE:
        return MatchesGetter.getFunctionMatch(diff, primaryFunction).getConfidence();
      case SIMILARITY:
        return MatchesGetter.getFunctionMatch(diff, primaryFunction).getSimilarity();
      case JUMP_MATCHES:
        return jumps;
      case SECONDARY_NAME:
        return secondaryFunction.getName();
      case SECONDARY_ADDRESS:
        return secondaryAddr.toHexString();
      default: // fall out
    }

    return null;
  }

  public void deleteDiff(Diff diff) {
    if (functionDiffViewList.remove(diff)) {
      fireTableDataChanged();
    }
  }
}
