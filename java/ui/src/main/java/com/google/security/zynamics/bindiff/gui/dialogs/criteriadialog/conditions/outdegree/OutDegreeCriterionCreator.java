package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.outdegree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;

public class OutDegreeCriterionCreator implements CriterionCreator {
  @Override
  public Criterion createCriterion() {
    return new OutDegreeCriterion();
  }

  @Override
  public String getCriterionDescription() {
    return "Select Nodes by Out-Degree";
  }
}
