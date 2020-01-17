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

package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

public class CheckboxCellEditor extends DefaultCellEditor implements ItemListener {
  private JCheckBox checkbox;

  public CheckboxCellEditor() {
    super(new JCheckBox());
  }

  @Override
  public Object getCellEditorValue() {
    checkbox.removeItemListener(this);

    return checkbox;
  }

  @Override
  public Component getTableCellEditorComponent(
      final JTable table,
      final Object value,
      final boolean isSelected,
      final int row,
      final int column) {
    if (value == null) {
      return null;
    }

    checkbox = (JCheckBox) value;
    checkbox.addItemListener(this);

    return (Component) value;
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    super.fireEditingStopped();
  }
}
