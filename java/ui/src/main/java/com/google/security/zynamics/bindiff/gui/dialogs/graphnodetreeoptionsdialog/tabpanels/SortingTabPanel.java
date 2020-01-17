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

package com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabpanels;

import com.google.common.collect.Lists;
import com.google.security.zynamics.bindiff.enums.ESortByCriterion;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabcomponents.SortingComboboxPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class SortingTabPanel extends JPanel {
  private static final int SORTING_LABEL_WIDTH = 100;
  private static final int SORTING_COMBOBOX_WIDTH = 150;
  private static final int ROW_HEIGHT = 25;

  private SortingComboboxPanel firstSortCombo;
  private SortingComboboxPanel secondSortCombo;
  private SortingComboboxPanel thirdSortCombo;
  private SortingComboboxPanel fourthSortCombo;
  private SortingComboboxPanel fifthSortCombo;

  private final boolean isCallGraphView;

  private final boolean isCombinedView;

  private ESortOrder initialFirstOrder;
  private ESortOrder initialSecondOrder;
  private ESortOrder initialThirdOrder;
  private ESortOrder initialFourthOrder;
  private ESortOrder initialFifthOrder;

  private ESortByCriterion initialFirstCriterion;
  private ESortByCriterion initialSecondCriterion;
  private ESortByCriterion initialThirdCriterion;
  private ESortByCriterion initialFourthCriterion;
  private ESortByCriterion initialFifthCriterion;

  public SortingTabPanel(final boolean isCombinedView, final boolean isCallGraphView) {
    super(new BorderLayout());

    add(createPanel(isCombinedView, isCallGraphView), BorderLayout.CENTER);

    this.isCallGraphView = isCallGraphView;
    this.isCombinedView = isCombinedView;

    setDefaults();
  }

  private JPanel createPanel(final boolean isCombinedView, final boolean isCallgraphView) {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new LineBorder(Color.GRAY));

    final JPanel innerPanel = new JPanel(new GridLayout(isCombinedView ? 6 : 5, 1, 2, 2));
    innerPanel.setBorder(new TitledBorder("Sort Options"));

    final List<String> sortingItems =
        Lists.newArrayList(
            ESortByCriterion.NONE.toString(),
            ESortByCriterion.ADDRESS.toString(),
            ESortByCriterion.MATCH_STATE.toString(),
            ESortByCriterion.SELECTION.toString(),
            ESortByCriterion.VISIBILITY.toString());

    if (isCombinedView) {
      sortingItems.add(ESortByCriterion.SIDE.toString());
    }

    if (isCallgraphView) {
      sortingItems.add(2, ESortByCriterion.FUNCTION_TYPE.toString());
      if (!isCombinedView) {
        sortingItems.add(3, ESortByCriterion.FUNCTION_NAME.toString());
      }
    }

    firstSortCombo =
        new SortingComboboxPanel(
            "First", sortingItems, SORTING_LABEL_WIDTH, SORTING_COMBOBOX_WIDTH, ROW_HEIGHT);
    secondSortCombo =
        new SortingComboboxPanel(
            "Second", sortingItems, SORTING_LABEL_WIDTH, SORTING_COMBOBOX_WIDTH, ROW_HEIGHT);
    thirdSortCombo =
        new SortingComboboxPanel(
            "Third", sortingItems, SORTING_LABEL_WIDTH, SORTING_COMBOBOX_WIDTH, ROW_HEIGHT);
    fourthSortCombo =
        new SortingComboboxPanel(
            "Fourth", sortingItems, SORTING_LABEL_WIDTH, SORTING_COMBOBOX_WIDTH, ROW_HEIGHT);
    fifthSortCombo =
        new SortingComboboxPanel(
            "Fifth", sortingItems, SORTING_LABEL_WIDTH, SORTING_COMBOBOX_WIDTH, ROW_HEIGHT);

    innerPanel.add(firstSortCombo);
    innerPanel.add(secondSortCombo);
    innerPanel.add(thirdSortCombo);
    innerPanel.add(fourthSortCombo);
    innerPanel.add(fifthSortCombo);

    panel.add(innerPanel, BorderLayout.NORTH);

    return panel;
  }

  public ESortByCriterion getSortByCriterion(final int sortCriterionDepth) {
    switch (sortCriterionDepth) {
      case 0:
        return ESortByCriterion.toSortCriterion(firstSortCombo.getValue());
      case 1:
        return ESortByCriterion.toSortCriterion(secondSortCombo.getValue());
      case 2:
        return ESortByCriterion.toSortCriterion(thirdSortCombo.getValue());
      case 3:
        return ESortByCriterion.toSortCriterion(fourthSortCombo.getValue());
      case 4:
        return ESortByCriterion.toSortCriterion(fifthSortCombo.getValue());
      default: // fall out
    }
    return ESortByCriterion.NONE;
  }

  public ESortOrder getSortOrder(final int sortCriterionDepth) {
    switch (sortCriterionDepth) {
      case 0:
        return firstSortCombo.getSortOrder();
      case 1:
        return secondSortCombo.getSortOrder();
      case 2:
        return thirdSortCombo.getSortOrder();
      case 3:
        return fourthSortCombo.getSortOrder();
      case 4:
        return fifthSortCombo.getSortOrder();
      default: // fall out
    }
    return ESortOrder.ASCENDING;
  }

  public void restoreInitialSettings() {
    firstSortCombo.setSelectItem(initialFirstCriterion.toString(), initialFirstOrder);
    secondSortCombo.setSelectItem(initialSecondCriterion.toString(), initialSecondOrder);
    thirdSortCombo.setSelectItem(initialThirdCriterion.toString(), initialThirdOrder);
    fourthSortCombo.setSelectItem(initialFourthCriterion.toString(), initialFourthOrder);
    fifthSortCombo.setSelectItem(initialFifthCriterion.toString(), initialFifthOrder);
  }

  public void setDefaults() {
    if (!isCallGraphView) {
      secondSortCombo.setSelectItem(ESortByCriterion.MATCH_STATE.toString(), ESortOrder.ASCENDING);

      if (!isCombinedView) {
        fourthSortCombo.setSelectItem(ESortByCriterion.ADDRESS.toString(), ESortOrder.ASCENDING);
        fifthSortCombo.setSelectItem(ESortByCriterion.NONE.toString(), ESortOrder.ASCENDING);
      } else {
        fourthSortCombo.setSelectItem(ESortByCriterion.SIDE.toString(), ESortOrder.ASCENDING);
        fifthSortCombo.setSelectItem(ESortByCriterion.ADDRESS.toString(), ESortOrder.ASCENDING);
      }
    } else {
      secondSortCombo.setSelectItem(
          ESortByCriterion.FUNCTION_TYPE.toString(), ESortOrder.ASCENDING);
      thirdSortCombo.setSelectItem(ESortByCriterion.MATCH_STATE.toString(), ESortOrder.ASCENDING);
      fifthSortCombo.setSelectItem(ESortByCriterion.ADDRESS.toString(), ESortOrder.ASCENDING);
    }
  }

  public void storeInitialSettings() {
    initialFirstOrder = firstSortCombo.getSortOrder();
    initialSecondOrder = secondSortCombo.getSortOrder();
    initialThirdOrder = thirdSortCombo.getSortOrder();
    initialFourthOrder = fourthSortCombo.getSortOrder();
    initialFifthOrder = fifthSortCombo.getSortOrder();

    initialFirstCriterion = getSortByCriterion(0);
    initialSecondCriterion = getSortByCriterion(1);
    initialThirdCriterion = getSortByCriterion(2);
    initialFourthCriterion = getSortByCriterion(3);
    initialFifthCriterion = getSortByCriterion(4);
  }
}
