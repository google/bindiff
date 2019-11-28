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
