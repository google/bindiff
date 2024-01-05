// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog;

import static com.google.common.base.Preconditions.checkNotNull;

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
    this.graphs = checkNotNull(graphs);
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
