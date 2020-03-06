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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.database.CommentsDatabase;
import com.google.security.zynamics.bindiff.database.MatchesDatabase;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.EViewType;
import com.google.security.zynamics.bindiff.exceptions.GraphCreationException;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.builders.ViewFlowGraphBuilder;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.detachedviewstabpanel.FunctionDiffViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelInitializer;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.builders.RawCombinedFlowGraphBuilder;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Triple;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashSet;

public class FlowGraphViewLoader extends CEndlessHelperThread {
  private final TabPanelManager tabPanelManager;
  private final MainWindow window;
  private final Workspace workspace;

  private final LinkedHashSet<Triple<Diff, IAddress, IAddress>> viewsAddrs;

  public FlowGraphViewLoader(
      final MainWindow window,
      final TabPanelManager tabPanelManager,
      final Workspace workspace,
      final LinkedHashSet<Triple<Diff, IAddress, IAddress>> viewsAddrs) {
    this.tabPanelManager = checkNotNull(tabPanelManager);
    this.window = checkNotNull(window);
    this.workspace = checkNotNull(workspace);
    this.viewsAddrs = checkNotNull(viewsAddrs);
  }

  private void createFlowGraphView(final FlowGraphViewData viewData) throws GraphCreationException {
    final Diff diff = viewData.getGraphs().getDiff();
    ViewTabPanel viewTabPanel;

    if (diff.isFunctionDiff()) {
      final FunctionMatchData functionMatch = diff.getMatches().getFunctionMatches()[0];

      viewTabPanel =
          new FunctionDiffViewTabPanel(
              window, tabPanelManager, workspace, diff, functionMatch, viewData);
    } else {
      viewTabPanel = new ViewTabPanel(window, tabPanelManager, workspace, diff, viewData);
    }

    try {
      ViewTabPanelInitializer.initialize(viewData.getGraphs(), this);
    } catch (final Exception e) {
      throw new GraphCreationException("An error occurred while initializing the graph.");
    }

    tabPanelManager.addTab(viewTabPanel);

    ViewTabPanelInitializer.centerSingleGraphs(viewData.getGraphs().getSuperGraph());
    ViewTabPanelInitializer.centerCombinedGraph(viewData.getGraphs(), viewTabPanel);

    tabPanelManager.selectTab(viewTabPanel);
  }

  private FlowGraphViewData loadFlowGraphViewData(
      final Diff diff, final IAddress priFunctionAddr, final IAddress secFunctionAddr)
      throws IOException, GraphCreationException, SQLException {
    // Load primary and secondary raw flow graphs.
    final CommentsDatabase database =
        workspace.getWorkspaceFile() != null ? new CommentsDatabase(workspace, true) : null;

    RawFlowGraph primaryRawFlowGraph = null;
    RawFlowGraph secondaryRawFlowGraph = null;
    final FunctionMatchData functionMatch =
        loadFunctionMatchData(diff, priFunctionAddr, secFunctionAddr);
    if (priFunctionAddr != null) {
      setDescription("Loading primary raw function data...");
      primaryRawFlowGraph =
          DiffLoader.loadRawFlowGraph(database, diff, priFunctionAddr, ESide.PRIMARY);
    }
    if (secFunctionAddr != null) {
      setDescription("Loading secondary raw function data...");
      secondaryRawFlowGraph =
          DiffLoader.loadRawFlowGraph(database, diff, secFunctionAddr, ESide.SECONDARY);
    }

    setDescription("Building combined flow graph...");
    final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
        combinedRawFlowGraph =
            RawCombinedFlowGraphBuilder.buildRawCombinedFlowGraph(
                functionMatch, primaryRawFlowGraph, secondaryRawFlowGraph);

    setDescription("Creating flow graph view...");
    final GraphsContainer graphs =
        ViewFlowGraphBuilder.buildViewFlowGraphs(diff, functionMatch, combinedRawFlowGraph);

    final FlowGraphViewData view =
        new FlowGraphViewData(
            primaryRawFlowGraph,
            secondaryRawFlowGraph,
            combinedRawFlowGraph,
            graphs,
            FlowGraphViewData.getViewName(graphs),
            EViewType.FUNCTION_DIFF_VIEW);

    diff.getViewManager().addView(view);

    return view;
  }

  private FunctionMatchData loadFunctionMatchData(
      final Diff diff, final IAddress priFunctionAddr, final IAddress secFunctionAddr)
      throws IOException {
    final FunctionMatchData functionMatch =
        diff.getMatches().getFunctionMatch(priFunctionAddr, ESide.PRIMARY);
    if (functionMatch != null) {
      try {
        String viewName = diff.getDiffName();
        if (!diff.isFunctionDiff()) {
          final RawFunction priFunction =
              diff.getCallGraph(ESide.PRIMARY).getFunction(priFunctionAddr);
          final RawFunction secFunction =
              diff.getCallGraph(ESide.SECONDARY).getFunction(secFunctionAddr);

          if (priFunction != null) {
            viewName = priFunction.getName();
          }

          if (secFunction != null) {
            viewName +=
                priFunction == null ? secFunction.getName() : " vs " + secFunction.getName();
          }
        }
        try (final MatchesDatabase matchesDb = new MatchesDatabase(diff.getMatchesDatabase())) {
          setGeneralDescription(String.format("Loading '%s'", viewName));
          setDescription("Please wait...");
          matchesDb.loadBasicBlockMatches(functionMatch);
        }
      } catch (final IOException | SQLException e) {
        throw new IOException(
            "Couldn't read flow graph basic block and instruction matches from database: "
                + e.getMessage());
      }
    }
    return functionMatch;
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    for (final Triple<Diff, IAddress, IAddress> viewAddrs : viewsAddrs) {
      final FlowGraphViewData viewData =
          loadFlowGraphViewData(viewAddrs.first(), viewAddrs.second(), viewAddrs.third());
      createFlowGraphView(viewData);
    }
  }
}
