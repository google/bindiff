package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionWrapper;
import java.util.List;
import javax.swing.JComboBox;

public class ConditionBox extends JComboBox<CriterionWrapper> {
  public ConditionBox(final List<CriterionCreator> criteria) {
    for (final CriterionCreator criterion : criteria) {
      addItem(new CriterionWrapper(criterion));
    }
  }
}
