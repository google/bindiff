package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.menus.NodeMenuBuilder;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

public class CriterionTreeNode extends AbstractCriterionTreeNode {
  private static final ImageIcon DEFAULT_ICON =
      ImageUtils.getImageIcon("data/selectbycriteriaicons/default-condition.png");

  private final NodeMenuBuilder menuBuilder;

  public CriterionTreeNode(
      final Criterion criterion,
      final List<CriterionCreator> criteria,
      final ExpressionTreeActionProvider actionProvider) {
    super(criterion);

    menuBuilder = new NodeMenuBuilder(this, criteria, actionProvider);
  }

  @Override
  public Icon getIcon() {
    final Icon icon = getCriterion().getIcon();

    if (icon == null) {
      return DEFAULT_ICON;
    }

    return icon;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return menuBuilder.getPopup();
  }

  @Override
  public String toString() {
    return getCriterion().getCriterionDescription();
  }
}
