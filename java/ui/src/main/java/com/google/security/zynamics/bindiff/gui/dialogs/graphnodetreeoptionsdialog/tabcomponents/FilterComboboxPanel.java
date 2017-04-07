package com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabcomponents;

import com.google.common.base.Preconditions;

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

    Preconditions.checkNotNull(labelText);
    Preconditions.checkNotNull(comboItems);

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
