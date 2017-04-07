package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.CriteriumType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriteriumTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.ICriteriumTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeValidator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ICriteriumTreeListener;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.JCriteriumTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriteriumTreeNode;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.CriteriumTreeNode;
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

  private final JCriteriumTree jtree;

  private final InternalTreeSelectionListener treeSelectionListener =
      new InternalTreeSelectionListener();

  private final InternalCriteriumTreeListener treeCriteriumlListener =
      new InternalCriteriumTreeListener();

  private final CriteriumTree ctree;

  private final JPanel defineConditionPanel;

  private final JButton addConditionButton;

  private final JButton okButton;

  public DialogUpdater(
      final JCriteriumTree tree,
      final CriteriumTree criteriumTree,
      final JPanel conditionPanel,
      final JButton conditionButton,
      final JButton okButton) {
    jtree = tree;
    ctree = criteriumTree;
    defineConditionPanel = conditionPanel;
    addConditionButton = conditionButton;
    this.okButton = okButton;

    jtree.addTreeSelectionListener(treeSelectionListener);

    ctree.addListener(treeCriteriumlListener);
  }

  private void updateDefineConditionPanel(final AbstractCriteriumTreeNode node) {
    Component component = null;

    if (node instanceof CriteriumTreeNode) {
      component = node.getCriterium().getCriteriumPanel();
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
    ctree.removeListener(treeCriteriumlListener);
  }

  private class InternalCriteriumTreeListener implements ICriteriumTreeListener {
    private void update() {
      updateInfoField();
      okButton.setEnabled(ExpressionTreeValidator.isValid(jtree));
    }

    private void updateCurrentCriteriumPath() {
      if (jtree.getSelectionPath() != null) {
        jtree.setCurrentCriteriumPath(jtree.getSelectionPath());
      } else {
        jtree.setCurrentCriteriumPath(jtree.getPathForRow(0));
      }

      updateDefineConditionPanel(
          (AbstractCriteriumTreeNode) jtree.getCurrentCriteriumPath().getLastPathComponent());
    }

    private void updateInfoField() {
      final Enumeration<?> nodes =
          ((AbstractCriteriumTreeNode) jtree.getModel().getRoot()).breadthFirstEnumeration();

      while (nodes.hasMoreElements()) {
        final AbstractCriteriumTreeNode node = (AbstractCriteriumTreeNode) nodes.nextElement();

        final JPanel panel = node.getCriterium().getCriteriumPanel();

        if (panel instanceof AbstractOperatorPanel) {
          final int count = node.getChildCount();

          final CriteriumType type = node.getCriterium().getType();

          final JTextArea infoField = ((AbstractOperatorPanel) panel).getInfoField();

          if (count == 1 && (type == CriteriumType.NOT || node.getLevel() == 0)
              || count > 1 && type != CriteriumType.NOT) {
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
        final CriteriumTree criteriumTree,
        final ICriteriumTreeNode parent,
        final ICriteriumTreeNode child) {
      update();
    }

    @Override
    public void nodeInserted(
        final CriteriumTree criteriumTree,
        final ICriteriumTreeNode parent,
        final ICriteriumTreeNode child) {
      update();
    }

    @Override
    public void nodeRemoved(final CriteriumTree criteriumTree, final ICriteriumTreeNode node) {
      updateCurrentCriteriumPath();
      update();
    }

    @Override
    public void removedAll(final CriteriumTree criteriumTree) {
      updateCurrentCriteriumPath();
      update();
    }
  }

  private class InternalTreeSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      final TreePath path = event.getPath();

      if (path == null) {
        addConditionButton.setEnabled(false);
        updateDefineConditionPanel((AbstractCriteriumTreeNode) jtree.getModel().getRoot());
      } else {
        final AbstractCriteriumTreeNode selectedNode =
            (AbstractCriteriumTreeNode) path.getLastPathComponent();

        boolean enable = selectedNode.allowAppend(CriteriumType.CONDITION);
        if (!enable) {
          if (selectedNode.getLevel() > 0) {
            final AbstractCriteriumTreeNode parentNode =
                (AbstractCriteriumTreeNode) selectedNode.getParent();

            if (parentNode.getLevel() != 0) {
              if (parentNode.getCriterium().getType() != CriteriumType.NOT
                  && selectedNode.getCriterium().getType() != CriteriumType.NOT) {
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
