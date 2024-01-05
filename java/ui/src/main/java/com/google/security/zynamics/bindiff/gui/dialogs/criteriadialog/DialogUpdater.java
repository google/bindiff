// Copyright 2011-2024 Google LLC
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

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.ICriterionTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeValidator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ICriterionTreeListener;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.JCriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriterionTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.CriterionTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators.AbstractOperatorPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Enumeration;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

public class DialogUpdater {
  private static final Color INVALID_OPERATOR_COLOR = new Color(160, 0, 0);

  private static final Color VALID_OPERATOR_COLOR = new Color(0, 0, 0);

  private final JCriterionTree jtree;

  private final InternalTreeSelectionListener treeSelectionListener =
      new InternalTreeSelectionListener();

  private final InternalCriterionTreeListener treeCriterionListener =
      new InternalCriterionTreeListener();

  private final CriterionTree ctree;

  private final JPanel defineConditionPanel;

  private final JButton addConditionButton;

  private final JButton okButton;

  public DialogUpdater(
      final JCriterionTree tree,
      final CriterionTree criterionTree,
      final JPanel conditionPanel,
      final JButton conditionButton,
      final JButton okButton) {
    jtree = tree;
    ctree = criterionTree;
    defineConditionPanel = conditionPanel;
    addConditionButton = conditionButton;
    this.okButton = okButton;

    jtree.addTreeSelectionListener(treeSelectionListener);

    ctree.addListener(treeCriterionListener);
  }

  private void updateDefineConditionPanel(final AbstractCriterionTreeNode node) {
    Component component = null;

    if (node instanceof CriterionTreeNode) {
      component = node.getCriterion().getCriterionPanel();
    }

    defineConditionPanel.removeAll();
    defineConditionPanel.setBorder(null);

    if (component != null) {
      defineConditionPanel.add(component);
    } else {
      final JPanel defaultPanel = new JPanel(new BorderLayout());
      defaultPanel.setBorder(new TitledBorder("Define Condition"));

      defineConditionPanel.add(defaultPanel, BorderLayout.CENTER);
    }

    defineConditionPanel.updateUI();
  }

  public void delete() {
    jtree.removeTreeSelectionListener(treeSelectionListener);
    ctree.removeListener(treeCriterionListener);
  }

  private class InternalCriterionTreeListener implements ICriterionTreeListener {
    private void update() {
      updateInfoField();
      okButton.setEnabled(ExpressionTreeValidator.isValid(jtree));
    }

    private void updateCurrentCriterionPath() {
      if (jtree.getSelectionPath() != null) {
        jtree.setCurrentCriterionPath(jtree.getSelectionPath());
      } else {
        jtree.setCurrentCriterionPath(jtree.getPathForRow(0));
      }

      updateDefineConditionPanel(
          (AbstractCriterionTreeNode) jtree.getCurrentCriterionPath().getLastPathComponent());
    }

    private void updateInfoField() {
      final Enumeration<?> nodes =
          ((AbstractCriterionTreeNode) jtree.getModel().getRoot()).breadthFirstEnumeration();

      while (nodes.hasMoreElements()) {
        final AbstractCriterionTreeNode node = (AbstractCriterionTreeNode) nodes.nextElement();

        final JPanel panel = node.getCriterion().getCriterionPanel();

        if (panel instanceof AbstractOperatorPanel) {
          final int count = node.getChildCount();

          final CriterionType type = node.getCriterion().getType();

          final JTextArea infoField = ((AbstractOperatorPanel) panel).getInfoField();

          if ((count == 1 && (type == CriterionType.NOT || node.getLevel() == 0))
              || (count > 1 && type != CriterionType.NOT)) {
            infoField.setForeground(VALID_OPERATOR_COLOR);
            infoField.setText(((AbstractOperatorPanel) panel).getValidInfoString());
          } else {
            infoField.setForeground(INVALID_OPERATOR_COLOR);
            infoField.setText(((AbstractOperatorPanel) panel).getInvalidInfoString());
          }
        }

        panel.updateUI();
      }
    }

    @Override
    public void nodeAppended(
        final CriterionTree criterionTree,
        final ICriterionTreeNode parent,
        final ICriterionTreeNode child) {
      update();
    }

    @Override
    public void nodeInserted(
        final CriterionTree criterionTree,
        final ICriterionTreeNode parent,
        final ICriterionTreeNode child) {
      update();
    }

    @Override
    public void nodeRemoved(final CriterionTree criterionTree, final ICriterionTreeNode node) {
      updateCurrentCriterionPath();
      update();
    }

    @Override
    public void removedAll(final CriterionTree criterionTree) {
      updateCurrentCriterionPath();
      update();
    }
  }

  private class InternalTreeSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      final TreePath path = event.getPath();

      if (path == null) {
        addConditionButton.setEnabled(false);
        updateDefineConditionPanel((AbstractCriterionTreeNode) jtree.getModel().getRoot());
      } else {
        final AbstractCriterionTreeNode selectedNode =
            (AbstractCriterionTreeNode) path.getLastPathComponent();

        boolean enable = selectedNode.allowAppend(CriterionType.CONDITION);
        if (!enable) {
          if (selectedNode.getLevel() > 0) {
            final AbstractCriterionTreeNode parentNode =
                (AbstractCriterionTreeNode) selectedNode.getParent();

            if (parentNode.getLevel() != 0) {
              if (parentNode.getCriterion().getType() != CriterionType.NOT
                  && selectedNode.getCriterion().getType() != CriterionType.NOT) {
                enable = true;
              }
            }
          }
        }

        addConditionButton.setEnabled(enable);
        updateDefineConditionPanel(selectedNode);
      }
    }
  }
}
