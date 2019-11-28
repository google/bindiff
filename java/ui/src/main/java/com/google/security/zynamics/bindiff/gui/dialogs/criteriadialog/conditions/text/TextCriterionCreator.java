package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.text;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;

public class TextCriterionCreator implements CriterionCreator {
  @Override
  public Criterion createCriterion() {
    return new TextCriterion();
  }

  @Override
  public String getCriterionDescription() {
    return "Select Nodes by Text";
  }
}
