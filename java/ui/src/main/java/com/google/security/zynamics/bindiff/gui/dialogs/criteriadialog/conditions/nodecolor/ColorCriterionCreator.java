package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.nodecolor;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.AbstractGraphsContainer;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.Criterion;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;

public class ColorCriterionCreator implements CriterionCreator {
  private final AbstractGraphsContainer graphs;

  public ColorCriterionCreator(final AbstractGraphsContainer graphs) {
    Preconditions.checkNotNull(graphs);

    this.graphs = graphs;
  }

  @Override
  public Criterion createCriterion() {
    return new ColorCriterion(graphs);
  }

  @Override
  public String getCriterionDescription() {
    return "Select Nodes by Color";
  }
}
