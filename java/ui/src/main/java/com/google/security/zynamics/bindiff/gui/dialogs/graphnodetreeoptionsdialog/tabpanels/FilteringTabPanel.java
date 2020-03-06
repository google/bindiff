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
import com.google.security.zynamics.bindiff.graph.filter.enums.EMatchStateFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.ESelectionFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.ESideFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.EVisibilityFilter;
import com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabcomponents.AddressRangeFieldPanel;
import com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabcomponents.FilterComboboxPanel;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class FilteringTabPanel extends JPanel {
  private static final int FILTER_LABEL_WIDTH = 100;
  private static final int FILTER_COMBOBOX_WIDTH = 150;
  private static final int ROW_HEIGHT = 25;

  private static final String FILTER_ITEM_NONE = "None";
  private static final String FILTER_ITEM_PRIMARY = "Primary";
  private static final String FILTER_ITEM_SECONRARY = "Secondary";

  private static final String FILTER_ITEM_SELECTED = "Selected";
  private static final String FILTER_ITEM_UNSELECTED = "Unselected";

  private static final String FILTER_ITEM_VISIBLE = "Visible";
  private static final String FILTER_ITEM_INVISIBLE = "Invisible";

  private static final String FILTER_ITEM_MATCHED = "Matched";
  private static final String FILTER_ITEM_MATCHED_IDENTICAL = "Matched Identical";
  private static final String FILTER_ITEM_MATCHED_INSTRUCTION_CHANGES = "Matched with Changes";
  private static final String FILTER_ITEM_MATCHED_INSTRUCTION_ONLY_CHANGES =
      "Matched with Instruction Changes Only";
  private static final String FILTER_ITEM_MATCHED_STRUCTURAL_CHANGES =
      "Matched with structural Changes";
  private static final String FILTER_ITEM_UNMATCHED = "Unmatched";

  private FilterComboboxPanel matchStateCombo;
  private FilterComboboxPanel selectionCombo;
  private FilterComboboxPanel visibilityCombo;
  private FilterComboboxPanel sideCombo;

  private AddressRangeFieldPanel startRangeField;
  private AddressRangeFieldPanel endRangeField;

  private String initialSelection;

  private String initialVisibility;

  private String initialMatchState;

  private String initialSide;

  private IAddress initialStartRange;

  private IAddress initialEndRange;

  public FilteringTabPanel(final boolean isCombinedView, final boolean isCallgraphView) {
    super(new BorderLayout());

    add(createPanel(isCombinedView, isCallgraphView), BorderLayout.CENTER);

    setDefaults();
  }

  private JPanel createPanel(final boolean isCombinedView, final boolean isCallgraphView) {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new LineBorder(Color.GRAY));

    final JPanel innerPanel = new JPanel(new GridLayout(isCombinedView ? 6 : 5, 1, 2, 2));
    innerPanel.setBorder(new TitledBorder("Filter Options"));

    final List<String> matchStateItems =
        Lists.newArrayList(FILTER_ITEM_NONE, FILTER_ITEM_MATCHED, FILTER_ITEM_UNMATCHED);
    final List<String> selectionItems =
        Lists.newArrayList(FILTER_ITEM_NONE, FILTER_ITEM_SELECTED, FILTER_ITEM_UNSELECTED);
    final List<String> visibilityItems =
        Lists.newArrayList(FILTER_ITEM_NONE, FILTER_ITEM_VISIBLE, FILTER_ITEM_INVISIBLE);
    final List<String> sideItems =
        Lists.newArrayList(FILTER_ITEM_NONE, FILTER_ITEM_PRIMARY, FILTER_ITEM_SECONRARY);

    if (isCallgraphView) {
      matchStateItems.add(2, FILTER_ITEM_MATCHED_IDENTICAL);
      matchStateItems.add(3, FILTER_ITEM_MATCHED_INSTRUCTION_ONLY_CHANGES);
      matchStateItems.add(4, FILTER_ITEM_MATCHED_STRUCTURAL_CHANGES);
    } else {
      matchStateItems.add(2, FILTER_ITEM_MATCHED_IDENTICAL);
      matchStateItems.add(3, FILTER_ITEM_MATCHED_INSTRUCTION_CHANGES);
    }

    matchStateCombo =
        new FilterComboboxPanel(
            "Match State", matchStateItems, FILTER_LABEL_WIDTH, FILTER_COMBOBOX_WIDTH, ROW_HEIGHT);
    selectionCombo =
        new FilterComboboxPanel(
            "Selection", selectionItems, FILTER_LABEL_WIDTH, FILTER_COMBOBOX_WIDTH, ROW_HEIGHT);
    sideCombo =
        new FilterComboboxPanel(
            "Side", sideItems, FILTER_LABEL_WIDTH, FILTER_COMBOBOX_WIDTH, ROW_HEIGHT);
    visibilityCombo =
        new FilterComboboxPanel(
            "Visibility", visibilityItems, FILTER_LABEL_WIDTH, FILTER_COMBOBOX_WIDTH, ROW_HEIGHT);
    startRangeField =
        new AddressRangeFieldPanel(
            "Start Range",
            new CAddress("0000000000000000", 16),
            FILTER_LABEL_WIDTH,
            FILTER_COMBOBOX_WIDTH,
            ROW_HEIGHT);
    endRangeField =
        new AddressRangeFieldPanel(
            "End Range",
            new CAddress("FFFFFFFFFFFFFFFF", 16),
            FILTER_LABEL_WIDTH,
            FILTER_COMBOBOX_WIDTH,
            ROW_HEIGHT);

    innerPanel.add(matchStateCombo);
    innerPanel.add(selectionCombo);
    innerPanel.add(visibilityCombo);
    if (isCombinedView) {
      innerPanel.add(sideCombo);
    }
    innerPanel.add(startRangeField);
    innerPanel.add(endRangeField);

    panel.add(innerPanel, BorderLayout.NORTH);

    return panel;
  }

  public IAddress getEndAddress() {
    return endRangeField.getValue();
  }

  public EMatchStateFilter getMatchStateFilter() {
    if (matchStateCombo.getValue().equals(FILTER_ITEM_MATCHED)) {
      return EMatchStateFilter.MATCHED;
    } else if (matchStateCombo.getValue().equals(FILTER_ITEM_UNMATCHED)) {
      return EMatchStateFilter.UNMATCHED;
    } else if (matchStateCombo.getValue().equals(FILTER_ITEM_MATCHED_IDENTICAL)) {
      return EMatchStateFilter.MATCHED_IDENTICAL;
    } else if (matchStateCombo.getValue().equals(FILTER_ITEM_MATCHED_INSTRUCTION_ONLY_CHANGES)) {
      return EMatchStateFilter.MATCHED_INSTRUCTION_CHANGES;
    } else if (matchStateCombo.getValue().equals(FILTER_ITEM_MATCHED_INSTRUCTION_CHANGES)) {
      return EMatchStateFilter.MATCHED_INSTRUCTION_CHANGES;
    } else if (matchStateCombo.getValue().equals(FILTER_ITEM_MATCHED_STRUCTURAL_CHANGES)) {
      return EMatchStateFilter.MATCHED_STRUTURAL_CHANGES;
    }

    return EMatchStateFilter.NONE;
  }

  public ESelectionFilter getSelectionFilter() {
    if (selectionCombo.getValue().equals(FILTER_ITEM_SELECTED)) {
      return ESelectionFilter.SELECTED;
    } else if (selectionCombo.getValue().equals(FILTER_ITEM_UNSELECTED)) {
      return ESelectionFilter.UNSELECTED;
    }

    return ESelectionFilter.NONE;
  }

  public ESideFilter getSideFilter() {
    if (sideCombo.getValue().equals(FILTER_ITEM_PRIMARY)) {
      return ESideFilter.PRIMARY;
    } else if (sideCombo.getValue().equals(FILTER_ITEM_SECONRARY)) {
      return ESideFilter.SECONDARY;
    }

    return ESideFilter.NONE;
  }

  public IAddress getStartAddress() {
    return startRangeField.getValue();
  }

  public EVisibilityFilter getVisibilityFilter() {
    if (visibilityCombo.getValue().equals(FILTER_ITEM_VISIBLE)) {
      return EVisibilityFilter.VISIBLE;
    } else if (visibilityCombo.getValue().equals(FILTER_ITEM_INVISIBLE)) {
      return EVisibilityFilter.INVISIBLE;
    }

    return EVisibilityFilter.NONE;
  }

  public void restoreInitialSettings() {
    selectionCombo.setValue(initialSelection);
    visibilityCombo.setValue(initialVisibility);
    matchStateCombo.setValue(initialMatchState);
    sideCombo.setValue(initialSide);

    startRangeField.setValue(initialStartRange);
    endRangeField.setValue(initialEndRange);
  }

  public void setDefaults() {
    selectionCombo.setValue(FILTER_ITEM_NONE);
    visibilityCombo.setValue(FILTER_ITEM_NONE);
    matchStateCombo.setValue(FILTER_ITEM_NONE);
    sideCombo.setValue(FILTER_ITEM_NONE);

    startRangeField.setDefault();
    endRangeField.setDefault();
  }

  public void storeInitialSettings() {
    initialSelection = selectionCombo.getValue();
    initialVisibility = visibilityCombo.getValue();
    initialMatchState = matchStateCombo.getValue();
    initialSide = sideCombo.getValue();

    initialStartRange = startRangeField.getValue();
    initialEndRange = endRangeField.getValue();
  }
}
