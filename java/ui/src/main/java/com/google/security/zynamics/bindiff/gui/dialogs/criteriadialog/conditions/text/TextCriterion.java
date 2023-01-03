// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.text;

import com.google.security.zynamics.bindiff.graph.searchers.NodeSearcher;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.ConditionCriterion;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class TextCriterion extends ConditionCriterion {
  private static final ImageIcon TEXT_CONDITION_ICON =
      ResourceUtils.getImageIcon("data/selectbycriteriaicons/text-condition.png");

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
