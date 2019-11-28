package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.indegrees;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;

public class IndegreeCriterionCreator implements CriterionCreator {
  @Override
  public Criterion createCriterion() {
    return new InDegreeCriterion();
  }

  @Override
  public String getCriterionDescription() {
    return "Select Nodes by Indegree";
  }
}
