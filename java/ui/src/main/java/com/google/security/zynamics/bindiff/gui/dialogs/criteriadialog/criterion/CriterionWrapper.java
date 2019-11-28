package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion;

import com.google.security.zynamics.zylib.gui.DefaultWrapper;

public class CriterionWrapper extends DefaultWrapper<CriterionCreator> {
  public CriterionWrapper(final CriterionCreator object) {
    super(object);
  }

  @Override
  public String toString() {
    return getObject().getCriterionDescription();
  }
}
