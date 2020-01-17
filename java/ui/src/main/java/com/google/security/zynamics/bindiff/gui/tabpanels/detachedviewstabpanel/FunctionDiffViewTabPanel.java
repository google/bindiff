// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.tabpanels.detachedviewstabpanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EMatchType;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.helpers.MatchesGetter;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class FunctionDiffViewTabPanel extends ViewTabPanel {
  private static final ImageIcon FUNCTIONDIFF_IDENTICAL_MATCHED_ICON =
      ImageUtils.getImageIcon("data/tabicons/functiondiff-flowgraphs-identical-matched-tab.png");
  private static final ImageIcon FUNCTIONDIFF_INSTRUCTION_CHANGED_ONLY_ICON =
      ImageUtils.getImageIcon(
          "data/tabicons/functiondiff-flowgraphs-changed-instructions-only-tab.png");
  private static final ImageIcon FUNCTIONDIFF_STRUTURAL_CHANGED_ICON =
      ImageUtils.getImageIcon("data/tabicons/functiondiff-flowgraphs_structural-changed-tab.png");
  private static final ImageIcon SAVED_FUNCTIONDIFF_IDENTICAL_MATCHED_ICON =
      ImageUtils.getImageIcon(
          "data/tabicons/savedfunctiondiff-flowgraphs-identical-matched-tab.png");
  private static final ImageIcon SAVED_FUNCTIONDIFF_INSTRUCTION_CHANGED_ONLY_ICON =
      ImageUtils.getImageIcon(
          "data/tabicons/savedfunctiondiffflowgraphs-changed-instructions-only-tab.png");
  private static final ImageIcon SAVED_FUNCTIONDIFF_STRUTURAL_CHANGED_ICON =
      ImageUtils.getImageIcon(
          "data/tabicons/savedfunctiondiffflowgraphs_structural-changed-tab.png");

  private final FunctionMatchData functionMatch;

  private final Workspace workspace;

  public FunctionDiffViewTabPanel(
      final MainWindow window,
      final TabPanelManager tabPanelManager,
      final Workspace workspace,
      final Diff diff,
      final FunctionMatchData functionMatch,
      final ViewData view) {
    super(window, tabPanelManager, workspace, diff, view);

    this.functionMatch = Preconditions.checkNotNull(functionMatch);
    this.workspace = Preconditions.checkNotNull(workspace);
  }

  private boolean isSavedView() {
    if (workspace.isLoaded()) {
      final String pathA =
          getDiff().getMatchesDatabase().getParentFile().getParentFile().getParent();
      final String pathB = workspace.getWorkspaceDirPath();

      return pathA.equals(pathB);
    }

    return false;
  }

  @Override
  public Icon getIcon() {
    final CombinedGraph combinedGraph = getView().getGraphs().getCombinedGraph();

    if (getView().isFlowgraphView()) {
      final EMatchType matchType =
          MatchesGetter.getFlowGraphsMatchType(combinedGraph, functionMatch);

      switch (matchType) {
        case IDENTICAL:
          return isSavedView()
              ? SAVED_FUNCTIONDIFF_IDENTICAL_MATCHED_ICON
              : FUNCTIONDIFF_IDENTICAL_MATCHED_ICON;
        case INSTRUCTIONS_CHANGED:
          return isSavedView()
              ? SAVED_FUNCTIONDIFF_INSTRUCTION_CHANGED_ONLY_ICON
              : FUNCTIONDIFF_INSTRUCTION_CHANGED_ONLY_ICON;
        case STRUCTURAL_CHANGED:
          return isSavedView()
              ? SAVED_FUNCTIONDIFF_STRUTURAL_CHANGED_ICON
              : FUNCTIONDIFF_STRUTURAL_CHANGED_ICON;
        default:
          return null;
      }
    }

    return null;
  }
}
