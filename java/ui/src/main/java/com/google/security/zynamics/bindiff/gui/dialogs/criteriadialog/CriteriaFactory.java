package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.AbstractGraphsContainer;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.indegrees.IndegreeCriteriumCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.nodecolor.ColorCriteriumCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.outdegree.OutdegreeCriteriumCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.recursion.RecursionCriteriumCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.selection.CSelectionCriteriumCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.text.TextCriteriumCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.visibillity.VisibilityCriteriumCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.ICriteriumCreator;
import java.util.ArrayList;
import java.util.List;

/** Provides all available individual criteria for the criteria dialog. */
public final class CriteriaFactory {
  private final AbstractGraphsContainer graphs;

  public CriteriaFactory(final AbstractGraphsContainer graphs) {
    this.graphs = Preconditions.checkNotNull(graphs);
  }

  public List<ICriteriumCreator> getConditions() {
    final List<ICriteriumCreator> conditions = new ArrayList<>(7);
    conditions.add(new TextCriteriumCreator());
    conditions.add(new ColorCriteriumCreator(graphs));
    conditions.add(new IndegreeCriteriumCreator());
    conditions.add(new OutdegreeCriteriumCreator());
    conditions.add(new RecursionCriteriumCreator());
    conditions.add(new VisibilityCriteriumCreator());
    conditions.add(new CSelectionCriteriumCreator());
    return conditions;
  }
}
