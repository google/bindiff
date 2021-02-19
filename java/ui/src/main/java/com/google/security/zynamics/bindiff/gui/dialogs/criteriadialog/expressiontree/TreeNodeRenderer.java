// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriterionTreeNode;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;

public class TreeNodeRenderer extends IconNodeRenderer {
  private static final Color VALID_NODE_FONT_COLOR = new Color(0, 0, 0);

  private static final Color INVALID_NODE_FONT_COLOR = new Color(160, 0, 0);

  @Override
  public Component getTreeCellRendererComponent(
      final JTree tree,
      final Object value,
      final boolean sel,
      final boolean expanded,
      final boolean leaf,
      final int row,
      final boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    if (value instanceof AbstractCriterionTreeNode) {
      final AbstractCriterionTreeNode node = (AbstractCriterionTreeNode) value;

      final int count = node.getChildCount();

      final CriterionType type = node.getCriterion().getType();

      if (type != CriterionType.CONDITION) {
        if ((count == 1 && (type == CriterionType.NOT || node.getLevel() == 0))
            || (count > 1 && type != CriterionType.NOT)) {
          setForeground(VALID_NODE_FONT_COLOR);
        } else {
          setForeground(INVALID_NODE_FONT_COLOR);
        }
      }
    }
    return this;
  }
}
