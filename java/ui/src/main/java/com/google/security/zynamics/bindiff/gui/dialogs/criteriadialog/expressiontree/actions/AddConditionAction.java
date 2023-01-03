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

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionWrapper;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;

public class AddConditionAction extends AbstractAction {
  private final CriterionCreator condition;

  private final ExpressionTreeActionProvider actionProvider;

  public AddConditionAction(
      final CriterionCreator condition, final ExpressionTreeActionProvider actionProvider) {
    super(condition.getCriterionDescription());

    this.condition = condition;
    this.actionProvider = actionProvider;
  }

  public AddConditionAction(
      final JComboBox<CriterionWrapper> selectionBox,
      final ExpressionTreeActionProvider actionProvider) {
    this.condition = ((CriterionWrapper) selectionBox.getSelectedItem()).getObject();
    this.actionProvider = actionProvider;

    if (condition != null) {
      putValue(NAME, condition.getCriterionDescription());
    }
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    actionProvider.appendCriterion(condition.createCriterion());
  }
}
