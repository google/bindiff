package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.AbstractCriterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;

public abstract class ConditionCriterion extends AbstractCriterion {
  @Override
  public CriterionType getType() {
    return CriterionType.CONDITION;
  }
}
