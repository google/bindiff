package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion;

public interface CriterionCreator {
  Criterion createCriterion();

  String getCriterionDescription();
}
