// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui;

import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.UrlLabel.UrlLabel;
import com.google.security.zynamics.zylib.resources.Constants;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;


public class CDialogAboutEx extends JDialog {
  private static final long serialVersionUID = -3626217728291899345L;

  public CDialogAboutEx(final Window owner, final ImageIcon logo, final String productName,
      final String message, final String description, final List<Pair<String, URL>> urls) {
    super(owner, "About " + productName, ModalityType.APPLICATION_MODAL);

    new CDialogEscaper(this);

    setLayout(new GridBagLayout());

    final GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 0;
    c.insets = new Insets(3, 3, 3, 3);

    final JPanel imagePanel = new JPanel();

    final JLabel piclabel = new JLabel();

    piclabel.setIcon(logo);

    imagePanel.add(piclabel);

    c.gridy = 0;

    add(imagePanel, c);

    final JPanel outerMessagePanel = new JPanel(new BorderLayout());
    outerMessagePanel.setBorder(new EmptyBorder(7, 7, 7, 7));


    final JPanel messagePanel = new JPanel(new BorderLayout());

    outerMessagePanel.add(messagePanel);

    messagePanel.setBorder(BorderFactory.createLoweredBevelBorder());

    final JTextArea messageTextField = new JTextArea(message);

    messageTextField.setBorder(new EmptyBorder(5, 5, 5, 5));
    messageTextField.setEditable(false);
    messageTextField.setBackground(messagePanel.getBackground());

    messagePanel.add(messageTextField);

    c.gridy = 1;

    add(outerMessagePanel, c);

    final JPanel outerDescriptionPanel = new JPanel(new BorderLayout());
    outerDescriptionPanel.setBorder(new EmptyBorder(0, 7, 7, 7));

    final JPanel descriptionPanel = new JPanel(new BorderLayout());
    outerDescriptionPanel.add(descriptionPanel);

    descriptionPanel.setBorder(BorderFactory.createLoweredBevelBorder());

    final JTextArea descriptionTextField = new JTextArea(description);

    descriptionTextField.setBorder(new EmptyBorder(5, 5, 5, 5));
    descriptionTextField.setEditable(false);
    descriptionTextField.setBackground(descriptionPanel.getBackground());

    descriptionPanel.add(descriptionTextField);

    c.gridy = 2;

    add(outerDescriptionPanel, c);


    final JPanel outerLowerPanel = new JPanel(new BorderLayout());
    outerLowerPanel.setBorder(new EmptyBorder(0, 7, 7, 7));

    final JPanel lowerPanel = new JPanel(new BorderLayout());
    outerLowerPanel.add(lowerPanel);

    final JPanel urlPanel = new JPanel();

    for (final Pair<String, URL> pair : urls) {
      final JLabel label = new UrlLabel(pair.first(), pair.second());

      label.setBorder(new EmptyBorder(0, 0, 0, 5));

      urlPanel.add(label);
    }

    lowerPanel.add(urlPanel, BorderLayout.WEST);

    final JButton okButton = new JButton("     " + Constants.OK + "     ");

    final JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
    buttonPanel.add(okButton);

    okButton.addActionListener(new InternalActionListener());

    lowerPanel.add(buttonPanel, BorderLayout.EAST);

    c.gridy = 3;

    add(outerLowerPanel, c);

    pack();

    setResizable(false);
  }

  private class InternalActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      dispose();
    }
  }
}
