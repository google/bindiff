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

package com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabcomponents;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class FilterComboboxPanel extends JPanel {
  private final JComboBox<String> combobox;

  private final JLabel label;

  public FilterComboboxPanel(
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

    init();
  }

  public String getValue() {
    return combobox.getSelectedItem().toString();
  }

  public void init() {
    setBorder(new EmptyBorder(1, 2, 2, 2));

    final JPanel panel = new JPanel(new BorderLayout());

    panel.add(label, BorderLayout.WEST);
    panel.add(combobox, BorderLayout.CENTER);

    add(panel, BorderLayout.CENTER);
  }

  public void setValue(final String item) {
    combobox.setSelectedItem(item);
  }
}
