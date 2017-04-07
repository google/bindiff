package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.comparators.RawFunctionTypeComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators.IconComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators.PercentageThreeBarCellDataComparator;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.helpers.GraphGetter;
import com.google.security.zynamics.bindiff.project.helpers.MatchesGetter;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.DoubleComparator;
import com.google.security.zynamics.zylib.general.comparators.HexStringComparator;
import com.google.security.zynamics.zylib.general.comparators.LexicalComparator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;

public class MatchedFunctionsViewsTableModel extends AbstractTableModel {
  public static final Icon IDENTICAL_MATCHED_ICON =
      ImageUtils.getImageIcon("data/tablecellicons/flowgraphs-identical-matched-tab.png");
  public static final Icon INSTRUCTIONS_CHANGED_ICON =
      ImageUtils.getImageIcon("data/tablecellicons/flowgraphs-changed-instructions-only-tab.png");
  public static final Icon STRUCTURAL_CHANGED_ICON =
      ImageUtils.getImageIcon("data/tablecellicons/flowgraphs_structural-changed-tab.png");

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

  private final ListenerProvider<IMatchedFunctionsViewsTableListener> listeners =
      new ListenerProvider<>();

  private final List<Pair<Integer, Comparator<?>>> sorters = new ArrayList<>();

  private final List<Pair<RawFunction, RawFunction>> matchedFunctionPairs = new ArrayList<>();

  public MatchedFunctionsViewsTableModel(final Diff diff, final boolean fillupTableData) {
    super(diff);

    if (fillupTableData) {
      final RawCallGraph priCallgraph = diff.getCallgraph(ESide.PRIMARY);
      final RawCallGraph secCallgraph = diff.getCallgraph(ESide.SECONDARY);

      setMatchedFunctionPairs(GraphGetter.getChangedFunctionPairs(priCallgraph, secCallgraph));
    }

    initSorters();
  }

  private Icon getIcon(final RawFunction primaryFunction, final RawFunction secondaryFunction) {
    if (MatchesGetter.isIdenticalFunctionPair(primaryFunction, secondaryFunction)) {
      return IDENTICAL_MATCHED_ICON;
    } else if (MatchesGetter.isStructuralChangedFunctionPair(primaryFunction, secondaryFunction)) {
      return STRUCTURAL_CHANGED_ICON;
    } else if (MatchesGetter.isInstructionsOnlyChangedFunctionPair(
        primaryFunction, secondaryFunction)) {
      return INSTRUCTIONS_CHANGED_ICON;
    }

    return null;
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

  public void addListener(final IMatchedFunctionsViewsTableListener tableListener) {
    listeners.addListener(tableListener);
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  @Override
  public String getColumnName(final int index) {
    return COLUMNS[index];
  }

  public Pair<RawFunction, RawFunction> getMatchedFunctionPairAt(final int index) {
    return matchedFunctionPairs.get(index);
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

    final IAddress primaryAddr = primaryFunction.getAddress();
    final IAddress secondaryAddr = secondaryFunction.getAddress();

    final int mBbs = primaryFunction.getSizeOfMatchedBasicblocks();
    final int pUBbs = primaryFunction.getSizeOfBasicblocks() - mBbs;
    final int sUBbs = secondaryFunction.getSizeOfBasicblocks() - mBbs;

    final int mJps = primaryFunction.getSizeOfMatchedJumps();
    final int pUJps = primaryFunction.getSizeOfJumps() - mJps;
    final int sUJps = secondaryFunction.getSizeOfJumps() - mJps;

    final PercentageThreeBarCellData basicblocks =
        new PercentageThreeBarCellData(pUBbs, mBbs, sUBbs, getColumnSortRelevance(col));
    final PercentageThreeBarCellData jumps =
        new PercentageThreeBarCellData(pUJps, mJps, sUJps, getColumnSortRelevance(col));

    switch (col) {
      case ICON:
        return getIcon(primaryFunction, secondaryFunction);
      case PRIMARY_ADDRESS:
        return primaryAddr.toHexString();
      case PRIMARY_NAME:
        return primaryFunction.getName();
      case PRIMARY_TYPE:
        return primaryFunction.getFunctionType();
      case BASICBLOCK_MATCHES:
        return basicblocks;
      case SIMILARITY:
        return MatchesGetter.getFunctionMatch(getDiff(), primaryFunction).getSimilarity();
      case CONFIDENCE:
        return MatchesGetter.getFunctionMatch(getDiff(), primaryFunction).getConfidence();
      case JUMP_MATCHES:
        return jumps;
      case SECONDARY_TYPE:
        return secondaryFunction.getFunctionType();
      case SECONDARY_NAME:
        return secondaryFunction.getName();
      case SECONDARY_ADDRESS:
        return secondaryAddr.toHexString();
      default: // fall out
    }

    return null;
  }

  public void removeListener(final IMatchedFunctionsViewsTableListener tableListener) {
    listeners.removeListener(tableListener);
  }

  public void setMatchedFunctionPairs(final Set<Pair<RawFunction, RawFunction>> functionPairs) {
    matchedFunctionPairs.clear();
    matchedFunctionPairs.addAll(functionPairs);

    fireTableDataChanged();

    for (final IMatchedFunctionsViewsTableListener listener : listeners) {
      listener.tableDataChanged(MatchedFunctionsViewsTableModel.this);
    }
  }
}
