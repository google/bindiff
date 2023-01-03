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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriterionTreeNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.tree.TreePath;

public class RemoveAction extends AbstractAction {
  private final AbstractCriterionTreeNode node;

  private final ExpressionTreeActionProvider actionProvider;

  public RemoveAction(
      final AbstractCriterionTreeNode node, final ExpressionTreeActionProvider actionProvider) {
    super("Remove");

    this.node = node;
    this.actionProvider = actionProvider;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    actionProvider.remove(new TreePath(node.getPath()));
  }
}
