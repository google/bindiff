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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import static com.google.common.base.Preconditions.checkNotNull;

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
    checkNotNull(diff);

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
