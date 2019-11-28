package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.ICriterionTreeNode;

public interface ICriterionTreeListener {
  void nodeAppended(
      CriterionTree criterionTree, ICriterionTreeNode parent, ICriterionTreeNode child);

  void nodeInserted(
      CriterionTree criterionTree, ICriterionTreeNode parent, ICriterionTreeNode child);

  void nodeRemoved(CriterionTree criterionTree, ICriterionTreeNode node);

  void removedAll(CriterionTree criterionTree);
}
