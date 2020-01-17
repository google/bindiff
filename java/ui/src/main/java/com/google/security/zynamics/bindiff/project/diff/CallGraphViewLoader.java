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

package com.google.security.zynamics.bindiff.project.diff;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.EViewType;
import com.google.security.zynamics.bindiff.exceptions.GraphCreationException;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.builders.ViewCallGraphBuilder;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelInitializer;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.builders.RawCombinedCallGraphBuilder;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedCallGraph;
import com.google.security.zynamics.bindiff.project.userview.CallGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;

public class CallGraphViewLoader extends CEndlessHelperThread {
  private Diff diff;

  private MainWindow window;
  private TabPanelManager tabPanelManager;
  private Workspace workspace;

  public CallGraphViewLoader(
      final Diff diff,
      final MainWindow window,
      final TabPanelManager tabPanelManager,
      final Workspace workspace) {
    this.diff = Preconditions.checkNotNull(diff);
    this.window = Preconditions.checkNotNull(window);
    this.tabPanelManager = Preconditions.checkNotNull(tabPanelManager);
    this.workspace = Preconditions.checkNotNull(workspace);
  }

  private void createCallgraphView(CallGraphViewData view) throws GraphCreationException {
    Preconditions.checkNotNull(tabPanelManager);
    Preconditions.checkNotNull(workspace);

    final ViewTabPanel viewTabPanel =
        new ViewTabPanel(window, tabPanelManager, workspace, diff, view);

    tabPanelManager.addTab(viewTabPanel);

    setDescription("Creating view...");

    try {
      ViewTabPanelInitializer.initialize(view.getGraphs(), this);
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      throw new GraphCreationException("An error occurred while initializing the graph.");
    }

    tabPanelManager.selectTab(viewTabPanel);

    view = null;
  }

  private CallGraphViewData loadCallgraphViewData() throws GraphCreationException {
    // Note: Primary and secondary raw callgraphs are already loaded with the diff itself.
    final RawCallGraph primaryCallgraph = diff.getCallGraph(ESide.PRIMARY);
    final RawCallGraph secondaryCallgraph = diff.getCallGraph(ESide.SECONDARY);

    setDescription("Building raw callgraph...");
    RawCombinedCallGraph combinedCallgraph =
        RawCombinedCallGraphBuilder.buildCombinedCallgraph(
            diff.getMatches(), primaryCallgraph, secondaryCallgraph);

    setDescription("Building view callgraph...");
    final GraphsContainer graphs =
        ViewCallGraphBuilder.buildDiffCallgraphs(diff, combinedCallgraph);

    final DiffMetaData meta = diff.getMetaData();
    final CallGraphViewData view =
        new CallGraphViewData(
            primaryCallgraph,
            secondaryCallgraph,
            graphs,
            FlowGraphViewData.getViewName(graphs),
            meta.getDisplayName(ESide.PRIMARY),
            meta.getDisplayName(ESide.SECONDARY),
            EViewType.FUNCTION_DIFF_VIEW);

    diff.getViewManager().addView(view);

    return view;
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    final CallGraphViewData view = loadCallgraphViewData();

    createCallgraphView(view);

    diff = null;
    tabPanelManager = null;
    workspace = null;
  }
}
