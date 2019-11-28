package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import java.util.List;

public interface ICriterionTreeNode {
  List<ICriterionTreeNode> getChildren();

  Criterion getCriterion();

  ICriterionTreeNode getParent();

  void setParent(ICriterionTreeNode parent);
}
