package com.google.security.zynamics.bindiff.gui.dialogs;

import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class SaveAsViewDialog extends BaseDialog {
  private final InternalButtonListener buttonListener = new InternalButtonListener();

  private final JTextField nameTextField = new JTextField();
  private final JTextArea commentTextField = new JTextArea();

  private final JButton okButton = new JButton("Ok");
  private final JButton cancelButton = new JButton("Cancel");

  private boolean pressedOkButton = false;

  public SaveAsViewDialog(final Window owner, final String defaultViewName) {
    super(owner, "Save View As");

    setModal(true);
    setLayout(new BorderLayout());

    nameTextField.setText(defaultViewName.toString());

    okButton.addActionListener(buttonListener);
    cancelButton.addActionListener(buttonListener);

    init();

    GuiHelper.centerChildToParent(owner, this, true);
  }

  private void init() {
    nameTextField.setPreferredSize(new Dimension(nameTextField.getPreferredSize().width, 25));

    commentTextField.setLineWrap(true);
    final JScrollPane scrollPane = new JScrollPane(commentTextField);

    final JPanel panel = new JPanel(new BorderLayout());

    final JPanel nameTextFieldPanel = new JPanel(new BorderLayout());
    nameTextFieldPanel.setBorder(new TitledBorder("View Name"));
    nameTextFieldPanel.add(nameTextField, BorderLayout.NORTH);

    final JPanel commentTextFieldPanel = new JPanel(new BorderLayout());
    commentTextFieldPanel.setBorder(new TitledBorder("View Comment"));
    commentTextFieldPanel.add(scrollPane, BorderLayout.CENTER);

    final JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.setBorder(new EmptyBorder(10, 5, 5, 5));

    final JPanel innerButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    innerButtonPanel.add(okButton);
    innerButtonPanel.add(cancelButton);

    buttonPanel.add(innerButtonPanel, BorderLayout.EAST);

    panel.add(nameTextFieldPanel, BorderLayout.NORTH);
    panel.add(commentTextFieldPanel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);

    pack();

    setPreferredSize(new Dimension(400, 250));
    setSize(new Dimension(400, 250));
    setMinimumSize(getSize());
  }

  @Override
  public void dispose() {
    okButton.removeActionListener(buttonListener);
    cancelButton.removeActionListener(buttonListener);

    super.dispose();
  }

  public boolean getOkButtonPressed() {
    return pressedOkButton;
  }

  public String getViewComment() {
    return commentTextField.getText();
  }

  public String getViewName() {
    return nameTextField.getText();
  }

  private class InternalButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(okButton)) {
        pressedOkButton = true;
      } else {
        pressedOkButton = false;
      }

      dispose();
    }
  }
}
