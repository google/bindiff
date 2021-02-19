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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.outdegree;

import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.zylib.gui.CDecFormatter;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/** User interface for setting out-degree conditions. */
public class OutDegreeCriterionPanel extends JPanel {
  private final OutDegreeCriterion criterion;

  private final JFormattedTextField inputField =
      TextComponentUtils.addDefaultEditorActions(new JFormattedTextField(new CDecFormatter(8)));

  private final JComboBox<String> operatorBox = new JComboBox<>();

  private final InternalComboboxListener comboboxListener = new InternalComboboxListener();

  private final InternalTextListener textFieldListener = new InternalTextListener();

  public OutDegreeCriterionPanel(final OutDegreeCriterion criterion) {
    super(new BorderLayout());

    this.criterion = criterion;

    operatorBox.addActionListener(comboboxListener);
    inputField.addKeyListener(textFieldListener);

    initPanel();
  }

  private void initPanel() {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Out-degree Condition"));

    operatorBox.addItem("<");
    operatorBox.addItem("=");
    operatorBox.addItem(">");

    final JPanel operatorPanel = new JPanel(new BorderLayout());
    operatorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    final JPanel inputPanel = new JPanel(new BorderLayout());
    inputPanel.setBorder(new EmptyBorder(5, 0, 5, 5));

    operatorPanel.add(operatorBox, BorderLayout.CENTER);
    inputPanel.add(inputField, BorderLayout.CENTER);

    final JPanel containerPanel = new JPanel(new BorderLayout());
    containerPanel.add(operatorPanel, BorderLayout.WEST);
    containerPanel.add(inputPanel, BorderLayout.CENTER);

    mainPanel.add(containerPanel, BorderLayout.NORTH);

    add(mainPanel, BorderLayout.CENTER);
  }

  public void delete() {
    operatorBox.removeActionListener(comboboxListener);
    inputField.removeKeyListener(textFieldListener);
  }

  public String getOperator() {
    return operatorBox.getSelectedItem().toString();
  }

  public int getOutDegree() {
    return inputField.getText().isEmpty() ? 0 : Integer.parseInt(inputField.getText());
  }

  private class InternalComboboxListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      criterion.update();
    }
  }

  private class InternalTextListener implements KeyListener {
    @Override
    public void keyPressed(final KeyEvent e) {
      criterion.update();
    }

    @Override
    public void keyReleased(final KeyEvent e) {
      criterion.update();
    }

    @Override
    public void keyTyped(final KeyEvent e) {
      criterion.update();
    }
  }
}
