package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.recursion;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;

public class RecursionCriterionCreator implements CriterionCreator {
  @Override
  public Criterion createCriterion() {
    return new RecursionCriterion();
  }

  @Override
  public String getCriterionDescription() {
    return "Select Nodes by Recursion";
  }
}
