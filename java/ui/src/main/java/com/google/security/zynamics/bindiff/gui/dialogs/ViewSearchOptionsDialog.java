package com.google.security.zynamics.bindiff.gui.dialogs;

import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class ViewSearchOptionsDialog extends BaseDialog {
  private static final ImageIcon APP_ICON =
      ImageUtils.getImageIcon("data/appicons/bindiff-16x16-rgba.png");

  private final InternalOkButtonListener okButtonListener = new InternalOkButtonListener();
  private final InternalCancelButtonListener cancelButtonListener =
      new InternalCancelButtonListener();

  private final JButton okButton = new JButton("Ok");

  private final JButton cancelButton = new JButton("Cancel");

  private final JCheckBox regExBox;

  private final JCheckBox caseSensitiveBox;

  private final JCheckBox primarySideBox;

  private final JCheckBox secondarySideBox;

  private final JCheckBox tempViewTables;

  private boolean okButtonPressed = false;

  public ViewSearchOptionsDialog(
      final Window parent,
      final String title,
      final boolean initialRegEx,
      final boolean initialCaseSensitive,
      final boolean initialPrimarySide,
      final boolean initialSecondarySide,
      final boolean initalTempTables) {
    super(parent, title);

    setIconImage(APP_ICON.getImage());

    okButton.addActionListener(okButtonListener);
    cancelButton.addActionListener(cancelButtonListener);

    regExBox = new JCheckBox("Regular Expression", initialRegEx);
    caseSensitiveBox = new JCheckBox("Case sensitive", initialCaseSensitive);
    primarySideBox = new JCheckBox("Primary Side", initialPrimarySide);
    secondarySideBox = new JCheckBox("Secondary Side", initialSecondarySide);
    tempViewTables =
        new JCheckBox("Only search in last temporary search result table", initalTempTables);

    init();

    GuiHelper.centerChildToParent(parent, this, true);
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));

    final JPanel leftCheckBoxPanel = new JPanel(new GridLayout(2, 1));
    leftCheckBoxPanel.setBorder(new TitledBorder(""));
    leftCheckBoxPanel.add(regExBox);
    leftCheckBoxPanel.add(caseSensitiveBox);

    final JPanel rightCheckBoxPanel = new JPanel(new GridLayout(2, 1));
    rightCheckBoxPanel.setBorder(new TitledBorder(""));
    rightCheckBoxPanel.add(primarySideBox);
    rightCheckBoxPanel.add(secondarySideBox);

    final JPanel checkBoxPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    checkBoxPanel.add(leftCheckBoxPanel);
    checkBoxPanel.add(rightCheckBoxPanel);

    final JPanel mainCheckBoxPanel = new JPanel(new BorderLayout());
    mainCheckBoxPanel.add(checkBoxPanel, BorderLayout.CENTER);
    final JPanel tempViewPanel = new JPanel(new BorderLayout());
    tempViewPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
    tempViewPanel.add(tempViewTables, BorderLayout.CENTER);
    mainCheckBoxPanel.add(tempViewPanel, BorderLayout.SOUTH);

    final JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
    final JPanel innerButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    innerButtonPanel.add(okButton);
    innerButtonPanel.add(cancelButton);
    buttonPanel.add(innerButtonPanel, BorderLayout.EAST);

    panel.add(mainCheckBoxPanel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    add(panel, BorderLayout.CENTER);

    pack();

    setMinimumSize(new Dimension(getPreferredSize().width, getPreferredSize().height));

    setResizable(false);
  }

  public void disableSideCheckboxes() {
    primarySideBox.setEnabled(false);
    secondarySideBox.setEnabled(false);
  }

  @Override
  public void dispose() {
    okButton.removeActionListener(okButtonListener);
    cancelButton.removeActionListener(cancelButtonListener);

    super.dispose();
  }

  public boolean getCaseSensitiveSelected() {
    return caseSensitiveBox.isSelected();
  }

  public boolean getOkButtonPressed() {
    return okButtonPressed;
  }

  public boolean getPrimarySideSearch() {
    return primarySideBox.isSelected();
  }

  public boolean getRegExSelected() {
    return regExBox.isSelected();
  }

  public boolean getSecondarySideSearch() {
    return secondarySideBox.isSelected();
  }

  public boolean getTemporaryTableUse() {
    return tempViewTables.isSelected();
  }

  private class InternalCancelButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      dispose();
    }
  }

  private class InternalOkButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      okButtonPressed = true;

      dispose();
    }
  }
}
