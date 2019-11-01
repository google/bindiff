package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.common.base.Preconditions;
import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.misc.EPercentageBarSortType;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.general.Pair;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class AbstractTableModel extends javax.swing.table.AbstractTableModel {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final Diff diff;

  private final Map<Integer, EPercentageBarSortType> sortRelevance = new HashMap<>();

  public AbstractTableModel() {
    diff = null;
  }

  public AbstractTableModel(final Diff diff) {
    Preconditions.checkNotNull(diff);

    this.diff = diff;
  }

  protected EPercentageBarSortType getColumnSortRelevance(final int column) {
    return sortRelevance.get(column);
  }

  public void dispose() {
    // Do nothing by default
  }

  @Override
  public abstract String getColumnName(final int index);

  public Diff getDiff() {
    return diff;
  }

  public abstract List<Pair<Integer, Comparator<?>>> getSorters();

  public void setColumnSortRelevance(final int column, final EPercentageBarSortType sortRelevance) {
    if (column >= getColumnCount()) {
      logger.at(Level.WARNING).log("Column is not in table");
      return;
    }
    this.sortRelevance.put(column, sortRelevance);
  }
}
