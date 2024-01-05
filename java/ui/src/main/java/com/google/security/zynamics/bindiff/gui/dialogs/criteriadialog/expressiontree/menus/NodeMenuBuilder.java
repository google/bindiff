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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.menus;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;
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
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.CriterionTreeNode;
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

  private final CriterionTreeNode criterionNode;

  public NodeMenuBuilder(
      final CriterionTreeNode criterionTreeNode,
      final List<CriterionCreator> criteria,
      final ExpressionTreeActionProvider actionProvider) {
    criterionNode = criterionTreeNode;

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

    for (final CriterionCreator condition : criteria) {
      conditionSubmenu.add(new JMenuItem(new AddConditionAction(condition, actionProvider)));
    }
    popup.add(conditionSubmenu);

    popup.add(new JSeparator());

    remove = new JMenuItem(new RemoveAction(criterionTreeNode, actionProvider));
    popup.add(remove);

    popup.add(new JSeparator());

    removeAll = new JMenuItem(new RemoveAllAction(actionProvider));
    popup.add(removeAll);
  }

  private void updateMenuState() {
    appendAnd.setEnabled(criterionNode.allowAppend(CriterionType.AND));
    appendOr.setEnabled(criterionNode.allowAppend(CriterionType.OR));
    appendNot.setEnabled(criterionNode.allowAppend(CriterionType.NOT));

    insertAnd.setEnabled(criterionNode.allowInsert(CriterionType.AND));
    insertOr.setEnabled(criterionNode.allowInsert(CriterionType.OR));
    insertNot.setEnabled(criterionNode.allowInsert(CriterionType.NOT));

    conditionSubmenu.setEnabled(criterionNode.allowAppend(CriterionType.CONDITION));

    remove.setEnabled(!criterionNode.isRoot());

    removeAll.setEnabled(criterionNode.getChildCount() != 0);
  }

  public JPopupMenu getPopup() {
    updateMenuState();

    return popup;
  }
}
