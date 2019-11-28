package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.selection;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class SelectionCriterionPanel extends JPanel {
  private final JComboBox<String> selectionBox = new JComboBox<>();

  private final InternalComboboxListener comboboxListener = new InternalComboboxListener();

  private final SelectionCriterion criterion;

  public SelectionCriterionPanel(final SelectionCriterion criterion) {
    super(new BorderLayout());

    this.criterion = criterion;

    selectionBox.addActionListener(comboboxListener);

    initPanel();
  }

  private void initPanel() {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Selection Condition"));

    final JPanel comboPanel = new JPanel(new BorderLayout());
    comboPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    selectionBox.addItem(SelectionState.SELECTED.toString());
    selectionBox.addItem(SelectionState.UNSELECTED.toString());

    comboPanel.add(selectionBox, BorderLayout.CENTER);

    mainPanel.add(comboPanel, BorderLayout.NORTH);

    add(mainPanel, BorderLayout.CENTER);
  }

  public void delete() {
    selectionBox.removeActionListener(comboboxListener);
  }

  public SelectionState getSelectionState() {
    return (SelectionState) selectionBox.getSelectedItem();
  }

  private class InternalComboboxListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      criterion.update();
    }
  }
}
