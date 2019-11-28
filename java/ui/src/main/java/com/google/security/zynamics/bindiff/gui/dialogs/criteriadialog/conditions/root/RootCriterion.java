package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.root;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.ConditionCriterion;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class RootCriterion extends ConditionCriterion {
  private static final ImageIcon ROOT_ICON =
      ImageUtils.getImageIcon("data/selectbycriteriaicons/root.png");

  private final RootCriterionPanel panel = new RootCriterionPanel();

  @Override
  public String getCriterionDescription() {
    return "Root Node";
  }

  @Override
  public JPanel getCriterionPanel() {
    return panel;
  }

  @Override
  public Icon getIcon() {
    return ROOT_ICON;
  }

  @Override
  public boolean matches(final ZyGraphNode<? extends CViewNode<?>> node) {
    return true;
  }

  public void update() {
    notifyListeners();
  }
}
