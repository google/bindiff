// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.indegrees;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.ConditionCriterion;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class InDegreeCriterion extends ConditionCriterion {
  private static final ImageIcon INDEGREE_CONDITION_ICON =
      ResourceUtils.getImageIcon("data/selectbycriteriaicons/indegree-condition.png");

  private final InDegreeCriterionPanel panel = new InDegreeCriterionPanel(this);

  @Override
  public String getCriterionDescription() {
    return String.format("Nodes with Indegree %s %d", getOperator(), getIndegree());
  }

  @Override
  public JPanel getCriterionPanel() {
    return panel;
  }

  @Override
  public Icon getIcon() {
    return INDEGREE_CONDITION_ICON;
  }

  public int getIndegree() {
    return panel.getIndegree();
  }

  public String getOperator() {
    return panel.getOperator();
  }

  @Override
  public boolean matches(final ZyGraphNode<? extends CViewNode<?>> node) {
    final String operator = panel.getOperator();

    final IViewNode<?> viewNode = node.getRawNode();

    if (operator.equals("<")) {
      return viewNode.getIncomingEdges().size() < getIndegree();
    }
    if (operator.equals("=")) {
      return viewNode.getIncomingEdges().size() == getIndegree();
    }
    if (operator.equals(">")) {
      return viewNode.getIncomingEdges().size() > getIndegree();
    }

    return false;
  }

  public void update() {
    notifyListeners();
  }
}
