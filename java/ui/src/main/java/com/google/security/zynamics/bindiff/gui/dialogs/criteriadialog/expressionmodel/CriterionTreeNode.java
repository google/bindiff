package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import java.util.ArrayList;
import java.util.List;

public class CriterionTreeNode implements ICriterionTreeNode {
  private final List<ICriterionTreeNode> children = new ArrayList<>();

  private ICriterionTreeNode parent;

  private final Criterion criterion;

  public CriterionTreeNode(final Criterion criterion) {
    this.criterion = criterion;
  }

  public static void append(final ICriterionTreeNode parent, final ICriterionTreeNode child) {
    parent.getChildren().add(child);
    child.setParent(parent);
  }

  public static void insert(final ICriterionTreeNode parent, final ICriterionTreeNode newChild) {
    for (final ICriterionTreeNode grandchild : parent.getChildren()) {
      newChild.getChildren().add(grandchild);
      grandchild.setParent(newChild);
    }

    parent.getChildren().clear();

    parent.getChildren().add(newChild);
    newChild.setParent(parent);
  }

  public static void remove(final ICriterionTreeNode node) {
    for (final ICriterionTreeNode child : node.getChildren()) {
      remove(child);
    }

    node.getChildren().clear();
  }

  @Override
  public List<ICriterionTreeNode> getChildren() {
    return children;
  }

  @Override
  public Criterion getCriterion() {
    return criterion;
  }

  @Override
  public ICriterionTreeNode getParent() {
    return parent;
  }

  @Override
  public void setParent(final ICriterionTreeNode parent) {
    this.parent = parent;
  }
}
