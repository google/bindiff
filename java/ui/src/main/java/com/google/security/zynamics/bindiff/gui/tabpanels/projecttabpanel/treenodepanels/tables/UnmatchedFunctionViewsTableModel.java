package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.comparators.RawFunctionTypeComparator;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators.PercentageTwoBarCellDataComparator;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.helpers.GraphGetter;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.HexStringComparator;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;
import com.google.security.zynamics.zylib.general.comparators.LexicalComparator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnmatchedFunctionViewsTableModel extends AbstractTableModel {
  public static final int ADDRESS = 0;
  public static final int FUNCTION_NAME = 1;
  public static final int TYPE = 2;
  public static final int BASICBLOCKS = 3;
  public static final int JUMPS = 4;
  public static final int INSTRUCTIONS = 5;
  public static final int CALLERS = 6;
  public static final int CALLEES = 7;

  private static final String[] COLUMNS = {
    "Address", "Name", "Type", "Basic Blocks", "Jumps", "Instructions", "Callers", "Callees",
  };

  private final ListenerProvider<IUnmatchedFunctionsViewsTableListener> listeners =
      new ListenerProvider<>();

  private final List<Pair<Integer, Comparator<?>>> sorters = new ArrayList<>();

  private final ESide side;

  private final List<RawFunction> unmatchedFunctions = new ArrayList<>();

  public UnmatchedFunctionViewsTableModel(
      final Diff diff, final ESide side, final boolean fillupTableData) {
    super(diff);

    this.side = side;

    if (fillupTableData) {
      final Set<RawFunction> functions = new HashSet<>();
      functions.addAll(GraphGetter.getUnmatchedFunctions(diff.getCallgraph(side)));
      setUnmatchedFunctions(functions);
    }

    initSorters();
  }

  private void initSorters() {
    sorters.add(new Pair<>(ADDRESS, new HexStringComparator()));
    sorters.add(new Pair<>(FUNCTION_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(TYPE, new RawFunctionTypeComparator()));
    sorters.add(new Pair<>(BASICBLOCKS, new IntComparator()));
    sorters.add(new Pair<>(JUMPS, new IntComparator()));
    sorters.add(new Pair<>(INSTRUCTIONS, new IntComparator()));
    sorters.add(new Pair<>(CALLERS, new IntComparator()));
    sorters.add(new Pair<>(CALLEES, new PercentageTwoBarCellDataComparator()));
  }

  public void addListener(final IUnmatchedFunctionsViewsTableListener listener) {
    listeners.addListener(listener);
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
    return unmatchedFunctions.size();
  }

  public ESide getSide() {
    return side;
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    return sorters;
  }

  public RawFunction getUnmatchedFunctionAt(final int index) {
    return unmatchedFunctions.get(index);
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    if (getRowCount() == 0) {
      return null;
    }

    final RawFunction unmatchedFunction = unmatchedFunctions.get(row);
    final IAddress address = unmatchedFunction.getAddress();

    switch (col) {
      case ADDRESS:
        return address.toHexString();
      case FUNCTION_NAME:
        return unmatchedFunction.getName();
      case TYPE:
        return unmatchedFunction.getFunctionType();
      case BASICBLOCKS:
        return unmatchedFunction.getSizeOfBasicblocks();
      case JUMPS:
        return unmatchedFunction.getSizeOfJumps();
      case INSTRUCTIONS:
        return unmatchedFunction.getSizeOfInstructions();
      case CALLERS:
        return unmatchedFunction.getCallers().size();
      case CALLEES:
        return unmatchedFunction.getCallees().size();
      default: // fall out
    }

    return null;
  }

  public void removeListener(final IUnmatchedFunctionsViewsTableListener listener) {
    listeners.removeListener(listener);
  }

  public void setUnmatchedFunctions(final Set<RawFunction> functionPairs) {
    unmatchedFunctions.clear();
    unmatchedFunctions.addAll(functionPairs);

    fireTableDataChanged();

    for (final IUnmatchedFunctionsViewsTableListener listener : listeners) {
      listener.tableDataChanged(UnmatchedFunctionViewsTableModel.this);
    }
  }
}
