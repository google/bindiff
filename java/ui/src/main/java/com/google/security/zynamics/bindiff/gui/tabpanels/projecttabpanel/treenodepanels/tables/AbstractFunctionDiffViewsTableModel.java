package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffListenerAdapter;
import com.google.security.zynamics.bindiff.project.diff.IDiffListener;
import java.util.List;

public abstract class AbstractFunctionDiffViewsTableModel extends AbstractTableModel {
  private final IDiffListener diffListener = new InternalDiffListener();

  List<Diff> functionDiffViewList;

  public AbstractFunctionDiffViewsTableModel(final List<Diff> functionDiffViewList) {
    Preconditions.checkNotNull(functionDiffViewList);

    this.functionDiffViewList = functionDiffViewList;

    addDiffListener();
  }

  private void addDiffListener() {
    for (final Diff diff : functionDiffViewList) {
      diff.addListener(diffListener);
    }
  }

  private void removeDiffListener() {
    for (final Diff diff : functionDiffViewList) {
      diff.removeListener(diffListener);
    }
  }

  public void addRow(final Diff diff) {
    if (functionDiffViewList.add(diff)) {
      diff.addListener(diffListener);
      fireTableDataChanged();
    }
  }

  @Override
  public void dispose() {
    removeDiffListener();
  }

  public Diff getDiffAt(final int row) {
    return functionDiffViewList.get(row);
  }

  @Override
  public int getRowCount() {
    return functionDiffViewList.size();
  }

  public void removeRow(final Diff diff) {
    if (functionDiffViewList.remove(diff)) {
      diff.removeListener(diffListener);
      fireTableDataChanged();
    }
  }

  public void setFunctionDiffList(final List<Diff> functionDiffList) {
    removeDiffListener();
    functionDiffViewList = functionDiffList;
    addDiffListener();
    fireTableDataChanged();
  }

  private class InternalDiffListener extends DiffListenerAdapter {
    private Diff isExisting(final String diffPath) {
      for (final Diff rowDiff : functionDiffViewList) {
        if (rowDiff.getMatchesDatabase().getPath().equals(diffPath)) {
          return rowDiff;
        }
      }

      return null;
    }

    @Override
    public void willOverwriteDiff(final String overwritePath) {
      final Diff diffToOverwrite = isExisting(overwritePath);
      if (diffToOverwrite != null) {
        removeRow(diffToOverwrite);
      }
    }
  }
}
