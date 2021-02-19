// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.CriterionTreeNode;
import java.util.Enumeration;

public class ExpressionTreeValidator {
  public static boolean isValid(final JCriterionTree tree) {
    final CriterionTreeNode root = (CriterionTreeNode) tree.getModel().getRoot();

    if (root.getChildCount() != 1) {
      return false;
    }

    final Enumeration<?> e = root.breadthFirstEnumeration();

    while (e.hasMoreElements()) {
      final CriterionTreeNode node = (CriterionTreeNode) e.nextElement();
      final CriterionType type = node.getCriterion().getType();
      final int count = node.getChildCount();

      if ((type == CriterionType.AND || type == CriterionType.OR) && count < 2) {
        return false;
      } else if (type == CriterionType.NOT && count != 1) {
        return false;
      }
    }
    return true;
  }
}
