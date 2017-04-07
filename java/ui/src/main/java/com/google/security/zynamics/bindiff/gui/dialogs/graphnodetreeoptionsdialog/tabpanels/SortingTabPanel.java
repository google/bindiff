package com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabpanels;

import com.google.common.collect.Lists;
import com.google.security.zynamics.bindiff.enums.ESortByCriterium;
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

  private final boolean isCallgraphView;

  private final boolean isCombinedView;

  private ESortOrder initialFirstOrder;
  private ESortOrder initialSecondOrder;
  private ESortOrder initialThirdOrder;
  private ESortOrder initialFourthOrder;
  private ESortOrder initialFifthOrder;

  private ESortByCriterium initialFirstCriterium;
  private ESortByCriterium initialSecondCriterium;
  private ESortByCriterium initialThirdCriterium;
  private ESortByCriterium initialFourthCriterium;
  private ESortByCriterium initialFifthCriterium;

  public SortingTabPanel(final boolean isCombinedView, final boolean isCallgraphView) {
    super(new BorderLayout());

    add(createPanel(isCombinedView, isCallgraphView), BorderLayout.CENTER);

    this.isCallgraphView = isCallgraphView;
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
            ESortByCriterium.NONE.toString(),
            ESortByCriterium.ADDRESS.toString(),
            ESortByCriterium.MATCHSTATE.toString(),
            ESortByCriterium.SELECTION.toString(),
            ESortByCriterium.VISIBILITY.toString());

    if (isCombinedView) {
      sortingItems.add(ESortByCriterium.SIDE.toString());
    }

    if (isCallgraphView) {
      sortingItems.add(2, ESortByCriterium.FUNCTIONTYPE.toString());
      if (!isCombinedView) {
        sortingItems.add(3, ESortByCriterium.FUNCTIONNAME.toString());
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

  public ESortByCriterium getSortByCriterium(final int sortCriteriumDepth) {
    switch (sortCriteriumDepth) {
      case 0:
        return ESortByCriterium.toSortCriterium(firstSortCombo.getValue());
      case 1:
        return ESortByCriterium.toSortCriterium(secondSortCombo.getValue());
      case 2:
        return ESortByCriterium.toSortCriterium(thirdSortCombo.getValue());
      case 3:
        return ESortByCriterium.toSortCriterium(fourthSortCombo.getValue());
      case 4:
        return ESortByCriterium.toSortCriterium(fifthSortCombo.getValue());
    }

    return ESortByCriterium.NONE;
  }

  public ESortOrder getSortOrder(final int sortCriteriumDepth) {
    switch (sortCriteriumDepth) {
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
    }

    return ESortOrder.ASCENDING;
  }

  public void restoreInitialSettings() {
    firstSortCombo.setSelectItem(initialFirstCriterium.toString(), initialFirstOrder);
    secondSortCombo.setSelectItem(initialSecondCriterium.toString(), initialSecondOrder);
    thirdSortCombo.setSelectItem(initialThirdCriterium.toString(), initialThirdOrder);
    fourthSortCombo.setSelectItem(initialFourthCriterium.toString(), initialFourthOrder);
    fifthSortCombo.setSelectItem(initialFifthCriterium.toString(), initialFifthOrder);
  }

  public void setDefaults() {
    if (!isCallgraphView) {
      secondSortCombo.setSelectItem(ESortByCriterium.MATCHSTATE.toString(), ESortOrder.ASCENDING);

      if (!isCombinedView) {
        fourthSortCombo.setSelectItem(ESortByCriterium.ADDRESS.toString(), ESortOrder.ASCENDING);
        fifthSortCombo.setSelectItem(ESortByCriterium.NONE.toString(), ESortOrder.ASCENDING);
      } else {
        fourthSortCombo.setSelectItem(ESortByCriterium.SIDE.toString(), ESortOrder.ASCENDING);
        fifthSortCombo.setSelectItem(ESortByCriterium.ADDRESS.toString(), ESortOrder.ASCENDING);
      }
    } else {
      secondSortCombo.setSelectItem(ESortByCriterium.FUNCTIONTYPE.toString(), ESortOrder.ASCENDING);
      thirdSortCombo.setSelectItem(ESortByCriterium.MATCHSTATE.toString(), ESortOrder.ASCENDING);
      fifthSortCombo.setSelectItem(ESortByCriterium.ADDRESS.toString(), ESortOrder.ASCENDING);
    }
  }

  public void storeInitialSettings() {
    initialFirstOrder = firstSortCombo.getSortOrder();
    initialSecondOrder = secondSortCombo.getSortOrder();
    initialThirdOrder = thirdSortCombo.getSortOrder();
    initialFourthOrder = fourthSortCombo.getSortOrder();
    initialFifthOrder = fifthSortCombo.getSortOrder();

    initialFirstCriterium = getSortByCriterium(0);
    initialSecondCriterium = getSortByCriterium(1);
    initialThirdCriterium = getSortByCriterium(2);
    initialFourthCriterium = getSortByCriterium(3);
    initialFifthCriterium = getSortByCriterium(4);
  }
}
