package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.selection;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;

public class SelectionCriterionCreator implements CriterionCreator {
  @Override
  public Criterion createCriterion() {
    return new SelectionCriterion();
  }

  @Override
  public String getCriterionDescription() {
    return "Select Nodes by Selection";
  }
}
