// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;
import java.util.Enumeration;
import javax.swing.JPopupMenu;

public abstract class AbstractCriterionTreeNode extends IconNode {
  private final Criterion criterion;

  public AbstractCriterionTreeNode(final Criterion criterion) {
    this.criterion = criterion;
  }

  private boolean allowAppendAndOrOperator() {
    final CriterionType type = getCriterion().getType();

    if (type != CriterionType.NOT) {
      return true;
    } else {
      return getChildCount() == 0;
    }
  }

  private boolean allowAppendCondition() {
    final int count = getChildCount();

    final CriterionType type = getCriterion().getType();

    if (type == CriterionType.CONDITION && isRoot() && count == 0) {
      return true;
    }

    if (type != CriterionType.NOT) {
      return true;
    } else {
      return getChildCount() == 0;
    }
  }

  private boolean allowAppendNotOperator() {
    final CriterionType type = getCriterion().getType();
    return type != CriterionType.NOT;
  }

  private boolean allowInsertAndOrOperator() {
    return getChildCount() > 0;
  }

  private boolean allowInsertCondition() {
    return false;
  }

  private boolean allowInsertNotOperator() {
    final CriterionType type = getCriterion().getType();

    final int count = getChildCount();

    if (type != CriterionType.NOT && count == 1) {
      return ((AbstractCriterionTreeNode) children.get(0)).getCriterion().getType()
          != CriterionType.NOT;
    }

    return false;
  }

  public boolean allowAppend(final CriterionType appendType) {
    if (isRoot() && getChildCount() > 0) {
      return false;
    }

    if (getCriterion().getType() == CriterionType.CONDITION && !isRoot()) {
      return false;
    }

    if (appendType == CriterionType.CONDITION) {
      return allowAppendCondition();
    }

    if (appendType == CriterionType.AND || appendType == CriterionType.OR) {
      return allowAppendAndOrOperator();
    }

    if (appendType == CriterionType.NOT) {
      return allowAppendNotOperator();
    }

    return false;
  }

  public boolean allowInsert(final CriterionType insertType) {
    if (getCriterion().getType() == CriterionType.CONDITION && !isRoot()) {
      return false;
    }

    if (insertType == CriterionType.CONDITION) {
      return allowInsertCondition();
    }

    if (insertType == CriterionType.AND || insertType == CriterionType.OR) {
      return allowInsertAndOrOperator();
    }

    if (insertType == CriterionType.NOT) {
      return allowInsertNotOperator();
    }

    return true;
  }

  public void deleteChildren() {
    final Enumeration<?> children = children();
    while (children.hasMoreElements()) {
      ((AbstractCriterionTreeNode) children.nextElement()).deleteChildren();
    }

    removeAllChildren();
  }

  public Criterion getCriterion() {
    return criterion;
  }

  public abstract JPopupMenu getPopupMenu();
}
