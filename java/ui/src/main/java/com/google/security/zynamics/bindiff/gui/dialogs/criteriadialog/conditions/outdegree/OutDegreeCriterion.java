package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.outdegree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.ConditionCriterion;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/** User interface for selecting nodes based on their out-degree. */
public class OutDegreeCriterion extends ConditionCriterion {
  private static final ImageIcon OUTDEGREE_CONDITION_ICON =
      ImageUtils.getImageIcon("data/selectbycriteriaicons/outdegree-condition.png");

  private final OutDegreeCriterionPanel panel = new OutDegreeCriterionPanel(this);

  @Override
  public String getCriterionDescription() {
    return String.format("Nodes with Outdegree %s %d", getOperator(), getOutDegree());
  }

  @Override
  public JPanel getCriterionPanel() {
    return panel;
  }

  @Override
  public Icon getIcon() {
    return OUTDEGREE_CONDITION_ICON;
  }

  public String getOperator() {
    return panel.getOperator();
  }

  public int getOutDegree() {
    return panel.getOutDegree();
  }

  @Override
  public boolean matches(final ZyGraphNode<? extends CViewNode<?>> node) {
    final String operator = panel.getOperator();

    final IViewNode<?> viewNode = node.getRawNode();

    if (operator.equals("<")) {
      return viewNode.getOutgoingEdges().size() < getOutDegree();
    }
    if (operator.equals("=")) {
      return viewNode.getOutgoingEdges().size() == getOutDegree();
    }
    if (operator.equals(">")) {
      return viewNode.getOutgoingEdges().size() > getOutDegree();
    }

    return false;
  }

  public void update() {
    notifyListeners();
  }
}
