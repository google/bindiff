package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.text;

import com.google.security.zynamics.bindiff.gui.components.TextComponentUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/** User interface for setting text-based conditions. */
public class TextCriterionPanel extends JPanel {
  private final JTextField inputField =
      TextComponentUtils.addDefaultEditorActions(new JTextField(""));

  private final JCheckBox caseSensitiveBox = new JCheckBox("Case Sensitive");

  private final JCheckBox regExBox = new JCheckBox("Regular Expression");

  private final TextCriterion criterion;

  public TextCriterionPanel(final TextCriterion criterion) {
    super(new BorderLayout());

    this.criterion = criterion;

    initPanel();
  }

  private void initPanel() {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Text Condition"));

    final JPanel inputPanel = new JPanel(new BorderLayout());
    inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    inputField.addKeyListener(new InternalTextListener());
    inputField.setPreferredSize(new Dimension(inputField.getPreferredSize().width, 23));
    inputPanel.add(inputField, BorderLayout.NORTH);

    final JPanel checkboxesPanel = new JPanel(new GridLayout(2, 1));
    checkboxesPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

    checkboxesPanel.add(caseSensitiveBox);
    checkboxesPanel.add(regExBox);

    inputPanel.add(checkboxesPanel, BorderLayout.CENTER);

    mainPanel.add(inputPanel, BorderLayout.NORTH);

    add(mainPanel, BorderLayout.CENTER);
  }

  public String getText() {
    return inputField.getText();
  }

  public boolean isCaseSensitive() {
    return caseSensitiveBox.isSelected();
  }

  public boolean isRegularExpression() {
    return regExBox.isSelected();
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
