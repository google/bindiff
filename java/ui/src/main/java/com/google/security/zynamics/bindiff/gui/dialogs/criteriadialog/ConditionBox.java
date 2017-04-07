package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.CriteriumWrapper;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.ICriteriumCreator;

import java.util.List;

import javax.swing.JComboBox;

public class ConditionBox extends JComboBox<String> {
  public ConditionBox(final List<ICriteriumCreator> criteria) {
    for (final ICriteriumCreator criterium : criteria) {
      addItem(new CriteriumWrapper(criterium).toString());
    }
  }
}
