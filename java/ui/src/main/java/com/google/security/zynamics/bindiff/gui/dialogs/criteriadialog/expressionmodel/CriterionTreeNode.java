// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
