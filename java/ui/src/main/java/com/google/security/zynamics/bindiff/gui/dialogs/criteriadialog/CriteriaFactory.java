package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.AbstractGraphsContainer;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.indegrees.IndegreeCriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.nodecolor.ColorCriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.outdegree.OutDegreeCriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.recursion.RecursionCriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.selection.SelectionCriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.text.TextCriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.visibillity.VisibilityCriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;
import java.util.ArrayList;
import java.util.List;

/** Provides all available individual criteria for the criteria dialog. */
public final class CriteriaFactory {
  private final AbstractGraphsContainer graphs;

  public CriteriaFactory(final AbstractGraphsContainer graphs) {
    this.graphs = Preconditions.checkNotNull(graphs);
  }

  public List<CriterionCreator> getConditions() {
    final List<CriterionCreator> conditions = new ArrayList<>(7);
    conditions.add(new TextCriterionCreator());
    conditions.add(new ColorCriterionCreator(graphs));
    conditions.add(new IndegreeCriterionCreator());
    conditions.add(new OutDegreeCriterionCreator());
    conditions.add(new RecursionCriterionCreator());
    conditions.add(new VisibilityCriterionCreator());
    conditions.add(new SelectionCriterionCreator());
    return conditions;
  }
}
