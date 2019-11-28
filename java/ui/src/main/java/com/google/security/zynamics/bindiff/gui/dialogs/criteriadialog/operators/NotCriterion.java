package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.AbstractCriterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class NotCriterion extends AbstractCriterion {
  private static final ImageIcon NOT_ICON =
      ImageUtils.getImageIcon("data/selectbycriteriaicons/not.png");

  private final NotCriterionPanel panel = new NotCriterionPanel();

  @Override
  public String getCriterionDescription() {
    return "NOT";
  }

  @Override
  public JPanel getCriterionPanel() {
    return panel;
  }

  @Override
  public Icon getIcon() {
    return NOT_ICON;
  }

  @Override
  public CriterionType getType() {
    return CriterionType.NOT;
  }

  @Override
  public boolean matches(final ZyGraphNode<? extends CViewNode<?>> node) {
    return true;
  }
}
