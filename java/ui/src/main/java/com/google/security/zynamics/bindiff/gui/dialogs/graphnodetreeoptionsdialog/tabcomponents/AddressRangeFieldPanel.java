// Copyright 2011-2023 Google LLC
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

import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.CHexFormatter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Panel with UI for entering address ranges. */
public class AddressRangeFieldPanel extends JPanel {
  private final JLabel label;

  private final JFormattedTextField textField;

  private final IAddress defaultAddress;

  public AddressRangeFieldPanel(
      final String labelText,
      final IAddress defaultAddress,
      final int labelWidth,
      final int textfieldWidth,
      final int height) {
    super(new BorderLayout());
    checkNotNull(labelText);
    checkNotNull(defaultAddress);

    this.defaultAddress = defaultAddress;

    label = new JLabel(labelText);
    textField =
        TextComponentUtils.addDefaultEditorActions(new JFormattedTextField(new CHexFormatter(16)));
    textField.setText(defaultAddress.toHexString());

    label.setPreferredSize(new Dimension(labelWidth, height));
    textField.setPreferredSize(new Dimension(textfieldWidth, height));

    init();
  }

  public IAddress getValue() {
    return new CAddress(textField.getText(), 16);
  }

  public void init() {
    setBorder(new EmptyBorder(2, 2, 2, 2));

    final JPanel panel = new JPanel(new BorderLayout());

    panel.add(label, BorderLayout.WEST);
    panel.add(textField, BorderLayout.CENTER);

    add(panel, BorderLayout.CENTER);
  }

  public void setDefault() {
    textField.setText(defaultAddress.toHexString());
  }

  public void setValue(final IAddress address) {
    textField.setValue(address.toHexString());
  }
}
