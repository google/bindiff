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

import com.google.security.zynamics.bindiff.enums.EExistence;
import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.comparators.RawFunctionTypeComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators.IconComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators.PercentageThreeBarCellDataComparator;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.helpers.MatchesGetter;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.Triple;
import com.google.security.zynamics.zylib.general.comparators.DoubleComparator;
import com.google.security.zynamics.zylib.general.comparators.HexStringComparator;
import com.google.security.zynamics.zylib.general.comparators.LexicalComparator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.Icon;

public class ExtendedMatchedFunctionViewsTableModel extends AbstractTableModel {
  public static final Icon ADDED_ICON = ResourceUtils.getImageIcon("data/tablecellicons/added.png");
  public static final Icon REMOVED_ICON =
      ResourceUtils.getImageIcon("data/tablecellicons/removed.png");

  public static final int ICON = 0;
  public static final int SIMILARITY = 1;
  public static final int CONFIDENCE = 2;
  public static final int PRIMARY_ADDRESS = 3;
  public static final int PRIMARY_NAME = 4;
  public static final int PRIMARY_TYPE = 5;
  public static final int SECONDARY_ADDRESS = 6;
  public static final int SECONDARY_NAME = 7;
  public static final int SECONDARY_TYPE = 8;
  public static final int BASICBLOCK_MATCHES = 9;
  public static final int JUMP_MATCHES = 10;

  private static final String[] COLUMNS = {
    "",
    "Similarity",
    "Confidence",
    "Address",
    "Primary Name",
    "Type",
    "Address",
    "Secondary Name",
    "Type",
    "Basic Blocks",
    "Jumps"
  };

  private final List<Pair<Integer, Comparator<?>>> sorters = new ArrayList<>();

  private final List<Triple<RawFunction, RawFunction, EExistence>> matchedFunctionPairs =
      new ArrayList<>();

  public ExtendedMatchedFunctionViewsTableModel(final Diff diff) {
    super(diff);
    initSorters();
  }

  private void initSorters() {
    sorters.add(new Pair<>(ICON, new IconComparator()));
    sorters.add(new Pair<>(PRIMARY_ADDRESS, new HexStringComparator()));
    sorters.add(new Pair<>(PRIMARY_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(PRIMARY_TYPE, new RawFunctionTypeComparator()));
    sorters.add(new Pair<>(BASICBLOCK_MATCHES, new PercentageThreeBarCellDataComparator()));
    sorters.add(new Pair<>(SIMILARITY, new DoubleComparator()));
    sorters.add(new Pair<>(CONFIDENCE, new DoubleComparator()));
    sorters.add(new Pair<>(JUMP_MATCHES, new PercentageThreeBarCellDataComparator()));
    sorters.add(new Pair<>(SECONDARY_TYPE, new RawFunctionTypeComparator()));
    sorters.add(new Pair<>(SECONDARY_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(SECONDARY_ADDRESS, new HexStringComparator()));
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
    return matchedFunctionPairs.size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    return sorters;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final RawFunction primaryFunction = matchedFunctionPairs.get(row).first();
    final RawFunction secondaryFunction = matchedFunctionPairs.get(row).second();
    final EExistence existance = matchedFunctionPairs.get(row).third();

    String priAddr = "";
    String secAddr = "";
    String priFunctionName = "";
    String secFunctionName = "";
    EFunctionType priFunctionType = EFunctionType.UNKNOWN;
    EFunctionType secFunctionType = EFunctionType.UNKNOWN;

    double similarity = -1;
    double confidence = -1;

    int mBbs = 0;
    int pUBbs = 0;
    int sUBbs = 0;

    int mJps = 0;
    int pUJps = 0;
    int sUJps = 0;

    PercentageThreeBarCellData basicblocks = null;
    PercentageThreeBarCellData jumps = null;

    if (primaryFunction != null && secondaryFunction != null) {
      similarity = MatchesGetter.getFunctionMatch(getDiff(), primaryFunction).getSimilarity();
      confidence = MatchesGetter.getFunctionMatch(getDiff(), primaryFunction).getConfidence();

      priAddr = primaryFunction.getAddress().toHexString();
      secAddr = secondaryFunction.getAddress().toHexString();
      priFunctionName = primaryFunction.getName();
      secFunctionName = secondaryFunction.getName();
      priFunctionType = primaryFunction.getFunctionType();
      secFunctionType = secondaryFunction.getFunctionType();

      mBbs = primaryFunction.getSizeOfMatchedBasicBlocks();
      pUBbs = primaryFunction.getSizeOfBasicBlocks() - mBbs;
      sUBbs = secondaryFunction.getSizeOfBasicBlocks() - mBbs;

      mJps = primaryFunction.getSizeOfMatchedJumps();
      pUJps = primaryFunction.getSizeOfJumps() - mJps;
      sUJps = secondaryFunction.getSizeOfJumps() - mJps;

      basicblocks = new PercentageThreeBarCellData(pUBbs, mBbs, sUBbs, getColumnSortRelevance(col));
      jumps = new PercentageThreeBarCellData(pUJps, mJps, sUJps, getColumnSortRelevance(col));
    } else if (primaryFunction != null) {
      priAddr = primaryFunction.getAddress().toHexString();
      priFunctionName = primaryFunction.getName();
      priFunctionType = primaryFunction.getFunctionType();
      pUBbs = primaryFunction.getSizeOfBasicBlocks();
      pUJps = primaryFunction.getSizeOfJumps();

      basicblocks = new PercentageThreeBarCellData(pUBbs, mBbs, sUBbs, getColumnSortRelevance(col));
      jumps = new PercentageThreeBarCellData(pUJps, mJps, sUJps, getColumnSortRelevance(col));
    } else if (secondaryFunction != null) {
      secAddr = secondaryFunction.getAddress().toHexString();
      secFunctionName = secondaryFunction.getName();
      secFunctionType = secondaryFunction.getFunctionType();
      sUBbs = secondaryFunction.getSizeOfBasicBlocks();
      sUJps = secondaryFunction.getSizeOfJumps();

      basicblocks = new PercentageThreeBarCellData(pUBbs, mBbs, sUBbs, getColumnSortRelevance(col));
      jumps = new PercentageThreeBarCellData(pUJps, mJps, sUJps, getColumnSortRelevance(col));
    } else {
      throw new IllegalStateException("Primary and secondary raw functions cannot both be null.");
    }

    switch (col) {
      case ICON:
        return existance == EExistence.ADDED ? ADDED_ICON : REMOVED_ICON;
      case PRIMARY_ADDRESS:
        return priAddr;
      case PRIMARY_NAME:
        return priFunctionName;
      case PRIMARY_TYPE:
        return priFunctionType;
      case BASICBLOCK_MATCHES:
        return basicblocks;
      case SIMILARITY:
        return similarity;
      case CONFIDENCE:
        return confidence;
      case JUMP_MATCHES:
        return jumps;
      case SECONDARY_TYPE:
        return secFunctionType;
      case SECONDARY_NAME:
        return secFunctionName;
      case SECONDARY_ADDRESS:
        return secAddr;
      default: // fall out
    }

    return null;
  }

  public void setMatchedFunctionPairs(
      final List<Triple<RawFunction, RawFunction, EExistence>> functionPairs) {
    matchedFunctionPairs.clear();
    matchedFunctionPairs.addAll(functionPairs);

    fireTableDataChanged();
  }
}
