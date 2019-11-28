package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.selection;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.ConditionCriterion;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class SelectionCriterion extends ConditionCriterion {
  private static final ImageIcon SELECTION_CONDITION_ICON =
      ImageUtils.getImageIcon("data/selectbycriteriaicons/selection-condition.png");

  private final SelectionCriterionPanel panel = new SelectionCriterionPanel(this);

  @Override
  public String getCriterionDescription() {
    return String.format(
        "%s Nodes",
        panel.getSelectionState() == SelectionState.SELECTED ? "Selected" : "Unselected");
  }

  @Override
  public JPanel getCriterionPanel() {
    return panel;
  }

  @Override
  public Icon getIcon() {
    return SELECTION_CONDITION_ICON;
  }

  @Override
  public boolean matches(final ZyGraphNode<? extends CViewNode<?>> node) {
    return node.getRawNode().isSelected() == (panel.getSelectionState() == SelectionState.SELECTED);
  }

  public void update() {
    notifyListeners();
  }
}
