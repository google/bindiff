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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public abstract class AbstractOperatorPanel extends JPanel {
  private final JTextArea infoField = new JTextArea();

  public AbstractOperatorPanel() {
    super(new BorderLayout());

    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder(getBorderTitle()));

    final JPanel infoPanel = new JPanel(new BorderLayout());
    infoPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    infoField.setBackground(infoPanel.getBackground());
    infoField.setLineWrap(true);
    infoField.setWrapStyleWord(true);
    infoField.setEditable(false);

    infoPanel.add(infoField, BorderLayout.CENTER);

    mainPanel.add(infoPanel, BorderLayout.CENTER);

    add(mainPanel, BorderLayout.CENTER);
  }

  public abstract String getBorderTitle();

  public JTextArea getInfoField() {
    return infoField;
  }

  public abstract String getInvalidInfoString();

  public abstract String getValidInfoString();
}
