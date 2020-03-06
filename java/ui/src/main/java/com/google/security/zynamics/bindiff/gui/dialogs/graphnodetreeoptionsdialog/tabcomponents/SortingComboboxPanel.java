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

package com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabcomponents;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESortOrder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class SortingComboboxPanel extends JPanel {
  private final JComboBox<String> combobox;

  private final JLabel label;

  private final JRadioButton ascSorting;
  private final JRadioButton descSorting;

  private final int height;

  public SortingComboboxPanel(
      final String labelText,
      final List<String> comboItems,
      final int labelWidth,
      final int comboWidth,
      final int height) {
    super(new BorderLayout());

    checkNotNull(labelText);
    checkNotNull(comboItems);

    label = new JLabel(labelText);
    final String[] comboItemsArray = new String[comboItems.size()];
    comboItems.toArray(comboItemsArray);
    combobox = new JComboBox<>(comboItemsArray);

    label.setPreferredSize(new Dimension(labelWidth, height));
    combobox.setPreferredSize(new Dimension(comboWidth, height));

    ascSorting = new JRadioButton("\u25B2");
    descSorting = new JRadioButton("\u25BC");

    ascSorting.setPreferredSize(new Dimension(ascSorting.getPreferredSize().width, height));
    descSorting.setPreferredSize(new Dimension(descSorting.getPreferredSize().width, height));

    ascSorting.setToolTipText("Ascending");
    descSorting.setToolTipText("Descending");

    ascSorting.setSelected(true);

    this.height = height;

    init();
  }

  private JPanel createSortSequencePane() {
    ascSorting.setForeground(Color.GRAY);
    descSorting.setForeground(Color.GRAY);
    ascSorting.setFocusable(false);
    descSorting.setFocusable(false);

    final JPanel panel = new JPanel(new GridLayout(1, 2));
    panel.setBorder(new TitledBorder(""));

    final ButtonGroup group = new ButtonGroup();
    group.add(ascSorting);
    group.add(descSorting);

    panel.add(ascSorting);
    panel.add(descSorting);

    final JPanel outterPanel = new JPanel(new BorderLayout());
    outterPanel.setBorder(new EmptyBorder(0, 1, 0, 0));
    outterPanel.add(panel, BorderLayout.CENTER);

    return outterPanel;
  }

  public ESortOrder getSortOrder() {
    if (ascSorting.isSelected()) {
      return ESortOrder.ASCENDING;
    }

    return ESortOrder.DESCENDING;
  }

  public String getValue() {
    return combobox.getSelectedItem().toString();
  }

  public void init() {
    setBorder(new EmptyBorder(2, 2, 2, 2));

    final JPanel panel = new JPanel(new BorderLayout());

    panel.add(label, BorderLayout.WEST);
    panel.add(combobox, BorderLayout.CENTER);
    panel.add(createSortSequencePane(), BorderLayout.EAST);

    panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, height));

    add(panel, BorderLayout.CENTER);
  }

  public void setSelectItem(final String item, final ESortOrder order) {
    ascSorting.setSelected(order == ESortOrder.ASCENDING);
    combobox.setSelectedItem(item);
  }
}
