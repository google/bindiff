package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.visibillity;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.ConditionCriterion;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class VisibilityCriterion extends ConditionCriterion {
  private static final ImageIcon VISIBILITY_CONDITION_ICON =
      ImageUtils.getImageIcon("data/selectbycriteriaicons/visibility-condition.png");

  private final VisibilityCriterionPanel panel = new VisibilityCriterionPanel(this);

  @Override
  public String getCriterionDescription() {
    return String.format(
        "%s Nodes",
        panel.getVisibilityState() == VisibilityState.VISIBLE ? "Visible" : "Invisible");
  }

  @Override
  public JPanel getCriterionPanel() {
    return panel;
  }

  @Override
  public Icon getIcon() {
    return VISIBILITY_CONDITION_ICON;
  }

  @Override
  public boolean matches(final ZyGraphNode<? extends CViewNode<?>> node) {
    return node.isVisible() == (panel.getVisibilityState() == VisibilityState.VISIBLE);
  }

  public void update() {
    notifyListeners();
  }
}
