package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriterionTreeNode;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;

public class TreeNodeRenderer extends IconNodeRenderer {
  private static final Color VALID_NODE_FONT_COLOR = new Color(0, 0, 0);

  private static final Color INVALID_NODE_FONT_COLOR = new Color(160, 0, 0);

  @Override
  public Component getTreeCellRendererComponent(
      final JTree tree,
      final Object value,
      final boolean sel,
      final boolean expanded,
      final boolean leaf,
      final int row,
      final boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    if (value instanceof AbstractCriterionTreeNode) {
      final AbstractCriterionTreeNode node = (AbstractCriterionTreeNode) value;

      final int count = node.getChildCount();

      final CriterionType type = node.getCriterion().getType();

      if (type != CriterionType.CONDITION) {
        if ((count == 1 && (type == CriterionType.NOT || node.getLevel() == 0))
            || (count > 1 && type != CriterionType.NOT)) {
          setForeground(VALID_NODE_FONT_COLOR);
        } else {
          setForeground(INVALID_NODE_FONT_COLOR);
        }
      }
    }
    return this;
  }
}
