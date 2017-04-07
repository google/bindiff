package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.CriteriumType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.CriteriumTreeNode;

import java.util.Enumeration;

public class ExpressionTreeValidator {
  public static boolean isValid(final JCriteriumTree tree) {
    final CriteriumTreeNode root = (CriteriumTreeNode) tree.getModel().getRoot();

    if (root.getChildCount() != 1) {
      return false;
    }

    final Enumeration<?> e = root.breadthFirstEnumeration();

    while (e.hasMoreElements()) {
      final CriteriumTreeNode node = (CriteriumTreeNode) e.nextElement();
      final CriteriumType type = node.getCriterium().getType();
      final int count = node.getChildCount();

      if ((type == CriteriumType.AND || type == CriteriumType.OR) && count < 2) {
        return false;
      } else if (type == CriteriumType.NOT && count != 1) {
        return false;
      }
    }
    return true;
  }
}
