// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.recursion;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class RecursionCriterionPanel extends JPanel {
  private final JComboBox<String> recursionBox = new JComboBox<>();

  private final InternalComboboxListener comboboxListener = new InternalComboboxListener();

  private final RecursionCriterion criterion;

  public RecursionCriterionPanel(final RecursionCriterion criterion) {
    super(new BorderLayout());

    this.criterion = criterion;

    recursionBox.addActionListener(comboboxListener);

    initPanel();
  }

  private void initPanel() {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Recursion Condition"));

    final JPanel comboPanel = new JPanel(new BorderLayout());
    comboPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    recursionBox.addItem(RecursionState.IS_RECURSION.toString());
    recursionBox.addItem(RecursionState.IS_NOT_RECURSION.toString());

    comboPanel.add(recursionBox, BorderLayout.CENTER);

    mainPanel.add(comboPanel, BorderLayout.NORTH);

    add(mainPanel, BorderLayout.CENTER);
  }

  public void delete() {
    recursionBox.removeActionListener(comboboxListener);
  }

  public RecursionState getRecursionState() {
    return (RecursionState) recursionBox.getSelectedItem();
  }

  private class InternalComboboxListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      criterion.update();
    }
  }
}
