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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffListener;
import java.util.List;

public abstract class AbstractFunctionDiffViewsTableModel extends AbstractTableModel {
  private final DiffListener diffListener = new InternalDiffListener();

  List<Diff> functionDiffViewList;

  public AbstractFunctionDiffViewsTableModel(final List<Diff> functionDiffViewList) {
    checkNotNull(functionDiffViewList);

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

  private class InternalDiffListener implements DiffListener {
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
