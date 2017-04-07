package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.menus;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.CriteriumType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.ICriteriumCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.AddConditionAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.AppendAndOperatorAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.AppendNotOperatorAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.AppendOrOperatorAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.InsertAndOperatorAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.InsertNotOperatorAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.InsertOrOperatorAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.RemoveAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.RemoveAllAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.CriteriumTreeNode;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class NodeMenuBuilder {
  private final JPopupMenu popup = new JPopupMenu();

  private final JMenuItem insertAnd;
  private final JMenuItem insertOr;
  private final JMenuItem insertNot;

  private final JMenuItem appendAnd;
  private final JMenuItem appendOr;
  private final JMenuItem appendNot;

  private final JMenu conditionSubmenu;

  private final JMenuItem remove;
  private final JMenuItem removeAll;

  private final CriteriumTreeNode criteriumNode;

  public NodeMenuBuilder(
      final CriteriumTreeNode criteriumTreeNode,
      final List<ICriteriumCreator> criteria,
      final ExpressionTreeActionProvider actionProvider) {
    criteriumNode = criteriumTreeNode;

    appendAnd = new JMenuItem(new AppendAndOperatorAction(actionProvider));
    appendOr = new JMenuItem(new AppendOrOperatorAction(actionProvider));
    appendNot = new JMenuItem(new AppendNotOperatorAction(actionProvider));

    popup.add(appendAnd);
    popup.add(appendOr);
    popup.add(appendNot);

    popup.add(new JSeparator());

    insertAnd = new JMenuItem(new InsertAndOperatorAction(actionProvider));
    insertOr = new JMenuItem(new InsertOrOperatorAction(actionProvider));
    insertNot = new JMenuItem(new InsertNotOperatorAction(actionProvider));

    popup.add(insertAnd);
    popup.add(insertOr);
    popup.add(insertNot);

    popup.add(new JSeparator());

    conditionSubmenu = new JMenu("Create Condition");

    for (final ICriteriumCreator condition : criteria) {
      conditionSubmenu.add(new JMenuItem(new AddConditionAction(condition, actionProvider)));
    }
    popup.add(conditionSubmenu);

    popup.add(new JSeparator());

    remove = new JMenuItem(new RemoveAction(criteriumTreeNode, actionProvider));
    popup.add(remove);

    popup.add(new JSeparator());

    removeAll = new JMenuItem(new RemoveAllAction(actionProvider));
    popup.add(removeAll);
  }

  private void updateMenuState() {
    appendAnd.setEnabled(criteriumNode.allowAppend(CriteriumType.AND));
    appendOr.setEnabled(criteriumNode.allowAppend(CriteriumType.OR));
    appendNot.setEnabled(criteriumNode.allowAppend(CriteriumType.NOT));

    insertAnd.setEnabled(criteriumNode.allowInsert(CriteriumType.AND));
    insertOr.setEnabled(criteriumNode.allowInsert(CriteriumType.OR));
    insertNot.setEnabled(criteriumNode.allowInsert(CriteriumType.NOT));

    conditionSubmenu.setEnabled(criteriumNode.allowAppend(CriteriumType.CONDITION));

    remove.setEnabled(!criteriumNode.isRoot());

    removeAll.setEnabled(criteriumNode.getChildCount() != 0);
  }

  public JPopupMenu getPopup() {
    updateMenuState();

    return popup;
  }
}
