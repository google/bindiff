// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.nodecolor;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.graph.AbstractGraphsContainer;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.ConditionCriterion;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class ColorCriterion extends ConditionCriterion {
  private static final ImageIcon COLOR_CONDITION_ICON =
      ResourceUtils.getImageIcon("data/selectbycriteriaicons/color-condition.png");

  private final ColorCriterionPanel panel;

  private final AbstractGraphsContainer graphs;

  public ColorCriterion(final AbstractGraphsContainer graphs) {
    checkNotNull(graphs);

    panel = new ColorCriterionPanel(this);

    this.graphs = graphs;
  }

  private JPanel getCriterionPanel(final AbstractGraphsContainer graphs) {
    panel.updateColors(graphs);

    return panel;
  }

  @Override
  public JPanel getCriterionPanel() {
    return getCriterionPanel(graphs);
  }

  public Color getColor() {
    return panel.getColor();
  }

  @Override
  public String getCriterionDescription() {
    return String.format("Nodes with Color %06X", getColor().getRGB() & 0xFFFFFF);
  }

  @Override
  public Icon getIcon() {
    return COLOR_CONDITION_ICON;
  }

  @Override
  public boolean matches(final ZyGraphNode<? extends CViewNode<?>> node) {
    return node.getRawNode().getColor().equals(getColor());
  }

  public void update() {
    notifyListeners();
  }
}
