package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.text;

import com.google.security.zynamics.bindiff.graph.searchers.NodeSearcher;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.ConditionCriterion;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class TextCriterion extends ConditionCriterion {
  private static final ImageIcon TEXT_CONDITION_ICON =
      ImageUtils.getImageIcon("data/selectbycriteriaicons/text-condition.png");

  private final TextCriterionPanel panel = new TextCriterionPanel(this);

  @Override
  public String getCriterionDescription() {
    return String.format("Nodes with Text '%s'", panel.getText());
  }

  @Override
  public JPanel getCriterionPanel() {
    return panel;
  }

  @Override
  public Icon getIcon() {
    return TEXT_CONDITION_ICON;
  }

  @Override
  public boolean matches(final ZyGraphNode<? extends CViewNode<?>> node) {
    return NodeSearcher.search(
                node, panel.getText(), panel.isRegularExpression(), panel.isCaseSensitive())
            .size()
        != 0;
  }

  public void update() {
    notifyListeners();
  }
}
