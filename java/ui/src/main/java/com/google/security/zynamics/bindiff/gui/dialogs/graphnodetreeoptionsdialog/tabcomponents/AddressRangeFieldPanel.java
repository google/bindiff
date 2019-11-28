package com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.tabcomponents;

import com.google.common.base.Preconditions;
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
    Preconditions.checkNotNull(labelText);
    Preconditions.checkNotNull(defaultAddress);

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
