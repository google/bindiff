// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.JCriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.CriterionTreeNode;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreeNode;

/**
 * Dialog class that provides the option to select nodes of a graph according to certain criteria
 * (node color, contains text XYZ, ...).
 */
public final class CriteriaDialog extends BaseDialog {
  private final CriterionTree ctree;

  private boolean selectNodes;

  private final DialogUpdater updater;

  public CriteriaDialog(final Window owner, final CriteriaFactory conditionFactory) {
    super(owner, "Select by Criteria");
    setModal(true);

    checkNotNull(conditionFactory);

    final List<CriterionCreator> criteria = conditionFactory.getConditions();

    ctree = new CriterionTree();

    final JCriterionTree jtree = new JCriterionTree(ctree, criteria);

    final ExpressionTreeActionProvider actionProvider =
        new ExpressionTreeActionProvider(jtree, ctree);

    final TreeNode rootNode =
        new CriterionTreeNode(ctree.getRootCriterion(), criteria, actionProvider);
    jtree.getModel().setRoot(rootNode);

    final ConditionBox selectionBox = new ConditionBox(criteria);

    final AddConditionButtonListener addConditionButtonListner =
        new AddConditionButtonListener(jtree, selectionBox, actionProvider);
    final JButton addConditionButton = new JButton(addConditionButtonListner);

    final CPanelTwoButtons okCancelPanel =
        new CPanelTwoButtons(new InternalOkCancelButtonListener(), "Execute", "Cancel");

    final JPanel defineConditionPanel = new JPanel(new BorderLayout());

    initDialog(owner, jtree, selectionBox, defineConditionPanel, okCancelPanel, addConditionButton);

    updater =
        new DialogUpdater(
            jtree, ctree, defineConditionPanel, addConditionButton, okCancelPanel.getFirstButton());
  }

  private void initDialog(
      final Window owner,
      final JCriterionTree jtree,
      final ConditionBox selectionBox,
      final JPanel defineConditionPanel,
      final CPanelTwoButtons okCancelPanel,
      final JButton addConditionButton) {
    final JPanel mainPanel = new JPanel(new BorderLayout());

    final JPanel dividerBorderPanel = new JPanel(new BorderLayout());
    dividerBorderPanel.setBorder(new EmptyBorder(2, 2, 2, 2));

    final JPanel dividerPanel = new JPanel(new GridLayout(1, 2));

    final JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setBorder(new TitledBorder("Expression Tree"));

    final JPanel rightPanel = new JPanel(new BorderLayout());
    final JPanel rightTopPanel = new JPanel(new BorderLayout());
    rightTopPanel.setBorder(new TitledBorder("Create Condition"));

    final JPanel rightTopComboPanel = new JPanel(new BorderLayout());
    rightTopComboPanel.setBorder(new EmptyBorder(1, 5, 5, 5));

    final JPanel rightTopAddPanel = new JPanel(new BorderLayout());
    rightTopAddPanel.setBorder(new EmptyBorder(1, 0, 5, 5));

    mainPanel.add(dividerBorderPanel, BorderLayout.CENTER);
    mainPanel.add(okCancelPanel, BorderLayout.SOUTH);
    okCancelPanel.getFirstButton().setEnabled(jtree.getSelectionPath() != null);

    dividerBorderPanel.add(dividerPanel, BorderLayout.CENTER);

    dividerPanel.add(leftPanel);
    dividerPanel.add(rightPanel);

    final JScrollPane pane = new JScrollPane(jtree);
    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    leftPanel.add(pane, BorderLayout.CENTER);

    defineConditionPanel.setBorder(new TitledBorder("Define Condition"));

    rightPanel.add(rightTopPanel, BorderLayout.NORTH);
    rightPanel.add(defineConditionPanel, BorderLayout.CENTER);

    rightTopPanel.add(rightTopComboPanel, BorderLayout.CENTER);
    rightTopPanel.add(rightTopAddPanel, BorderLayout.EAST);

    rightTopComboPanel.add(selectionBox, BorderLayout.CENTER);

    addConditionButton.setText("Add");

    addConditionButton.setEnabled(false);
    rightTopAddPanel.add(addConditionButton, BorderLayout.CENTER);

    add(mainPanel);

    setIconImage(null);

    pack();

    GuiHelper.centerChildToParent(owner, this, true);
  }

  public void delete() {
    updater.delete();
  }

  public boolean doSelectNodes() {
    return selectNodes;
  }

  public CriterionTree getCriterionTree() {
    return ctree;
  }

  private class InternalOkCancelButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      selectNodes = e.getActionCommand().equals("Execute");

      dispose();
    }
  }
}
