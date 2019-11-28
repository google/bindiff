package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.visibillity;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;

public class VisibilityCriterionCreator implements CriterionCreator {
  @Override
  public Criterion createCriterion() {
    return new VisibilityCriterion();
  }

  @Override
  public String getCriterionDescription() {
    return "Select Nodes by Visibility";
  }
}
