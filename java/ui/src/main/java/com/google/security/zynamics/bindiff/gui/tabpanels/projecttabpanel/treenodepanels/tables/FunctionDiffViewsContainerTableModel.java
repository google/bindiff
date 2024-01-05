// Copyright 2011-2024 Google LLC
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
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.DateComparator;
import com.google.security.zynamics.zylib.general.comparators.LexicalComparator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FunctionDiffViewsContainerTableModel extends AbstractFunctionDiffViewsTableModel {
  public static final int PRIMARY_IMAGE_NAME = 0;
  public static final int PRIMARY_IMAGE_HASH = 1;
  public static final int SECONDARY_IMAGE_NAME = 2;
  public static final int SECONDARY_IMAGE_HASH = 3;
  public static final int VIEW_NAME = 4;
  public static final int CREATION_DATE = 5;

  private static final String[] COLUMNS = {
    "Primary Image Name",
    "Primary Image Hash",
    "Secondary Image Name",
    "Secondary Image Hash",
    "View Name",
    "Creation Date"
  };

  private final List<Pair<Integer, Comparator<?>>> sorters = new ArrayList<>();

  public FunctionDiffViewsContainerTableModel(final List<Diff> functionDiffViewList) {
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
    sorters.add(new Pair<>(PRIMARY_IMAGE_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(PRIMARY_IMAGE_HASH, new LexicalComparator()));
    sorters.add(new Pair<>(SECONDARY_IMAGE_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(SECONDARY_IMAGE_HASH, new LexicalComparator()));
    sorters.add(new Pair<>(VIEW_NAME, new LexicalComparator()));
    sorters.add(new Pair<>(CREATION_DATE, new DateComparator()));

    return sorters;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final Diff diff = functionDiffViewList.get(row);
    final DiffMetadata metaData = diff.getMetadata();

    final String viewName =
        BinDiffFileUtils.forceFilenameEndsNotWithExtension(
            diff.getMatchesDatabase().getName(), Constants.BINDIFF_MATCHES_DB_EXTENSION);

    final Date creationDate = metaData.getDate().getTime();

    switch (col) {
      case PRIMARY_IMAGE_NAME:
        return metaData.getImageName(ESide.PRIMARY);
      case PRIMARY_IMAGE_HASH:
        return metaData.getImageHash(ESide.PRIMARY);
      case SECONDARY_IMAGE_NAME:
        return metaData.getImageName(ESide.SECONDARY);
      case SECONDARY_IMAGE_HASH:
        return metaData.getImageHash(ESide.SECONDARY);
      case VIEW_NAME:
        return viewName;
      case CREATION_DATE:
        return creationDate;
      default: // fall out
    }

    return null;
  }
}
