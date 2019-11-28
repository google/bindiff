package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.root;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;

public class RootCriterionCreator implements CriterionCreator {
  @Override
  public Criterion createCriterion() {
    return new RootCriterion();
  }

  @Override
  public String getCriterionDescription() {
    return "Root Node";
  }
}
