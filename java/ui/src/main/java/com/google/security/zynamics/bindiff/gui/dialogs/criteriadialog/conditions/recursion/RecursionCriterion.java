package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.recursion;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.ConditionCriterion;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class RecursionCriterion extends ConditionCriterion {
  private static final ImageIcon RECURSION_CONDITION_ICON =
      ImageUtils.getImageIcon("data/selectbycriteriaicons/recursion-condition.png");

  private final RecursionCriterionPanel panel = new RecursionCriterionPanel(this);

  @Override
  public String getCriterionDescription() {
    return String.format(
        "Nodes with %s Recursion",
        panel.getRecursionState() == RecursionState.IS_RECURSION ? "" : "no");
  }

  @Override
  public JPanel getCriterionPanel() {
    return panel;
  }

  @Override
  public Icon getIcon() {
    return RECURSION_CONDITION_ICON;
  }

  @Override
  public boolean matches(final ZyGraphNode<? extends CViewNode<?>> node) {
    if (panel.getRecursionState() == RecursionState.IS_RECURSION) {
      return node.getChildren().contains(node);
    } else {
      return node.getChildren().contains(node);
    }
  }

  public void update() {
    notifyListeners();
  }
}
