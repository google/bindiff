// Copyright 2011-2021 Google LLC
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
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelInitializer;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.io.matches.DiffRequestMessage;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.builders.RawCombinedFlowGraphBuilder;
import com.google.security.zynamics.bindiff.project.matches.FunctionDiffMetadata;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.BinDiffFileUtils;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import java.io.File;

/** Loads flow graphs into a new view. */
public class FunctionDiffViewLoader extends CEndlessHelperThread {
  private final DiffRequestMessage data;
  private final MainWindow window;
  private final TabPanelManager tabPanelManager;
  private final Workspace workspace;

  private FlowGraphViewData viewData;

  public FunctionDiffViewLoader(
      final DiffRequestMessage data,
      final MainWindow window,
      final TabPanelManager tabPanelManager,
      final Workspace workspace) {
    this.data = checkNotNull(data);
    this.window = checkNotNull(window);
    this.tabPanelManager = checkNotNull(tabPanelManager);
    this.workspace = checkNotNull(workspace);
    this.viewData = null;
  }

  private void createSingleFunctionDiffFlowGraphView(
      final Diff diff,
      final FunctionMatchData functionMatch,
      final RawFlowGraph priFlowGraph,
      final RawFlowGraph secFlowGraph,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedFlowGraph)
      throws GraphCreationException {
    final GraphsContainer graphs =
        ViewFlowGraphBuilder.buildViewFlowGraphs(diff, functionMatch, combinedFlowGraph);

    final RawFunction priFunction =
        diff.getFunction(functionMatch.getIAddress(ESide.PRIMARY), ESide.PRIMARY);
    final RawFunction secFunction =
        diff.getFunction(functionMatch.getIAddress(ESide.SECONDARY), ESide.SECONDARY);

    String name = diff.getMatchesDatabase().getName();
    name =
        BinDiffFileUtils.forceFilenameEndsNotWithExtension(
            name, Constants.BINDIFF_MATCHES_DB_EXTENSION);

    if (!workspace.isLoaded()
        || diff.getMatchesDatabase().getParent().indexOf(workspace.getWorkspaceDirPath()) != 0) {
      name = String.format("%s vs %s", priFunction.getName(), secFunction.getName());
    }

    viewData =
        new FlowGraphViewData(
            priFlowGraph,
            secFlowGraph,
            combinedFlowGraph,
            graphs,
            name,
            EViewType.SINGLE_FUNCTION_DIFF_VIEW);

    diff.getViewManager().addView(viewData);

    final FunctionDiffViewTabPanel viewTabPanel =
        new FunctionDiffViewTabPanel(
            window, tabPanelManager, workspace, diff, functionMatch, viewData);

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

  @Override
  protected void runExpensiveCommand() throws Exception {
    final File matchesFile = new File(data.getMatchesDBPath());

    try (final MatchesDatabase matchesDB = new MatchesDatabase(matchesFile)) {
      final FunctionDiffMetadata metaData = matchesDB.loadFunctionDiffMetadata(true);

      final File primaryExportFile = new File(data.getBinExportPath(ESide.PRIMARY));
      final File secondaryExportFile = new File(data.getBinExportPath(ESide.SECONDARY));

      Diff diff = data.getDiff();
      if (diff == null) {
        diff = new Diff(metaData, matchesFile, primaryExportFile, secondaryExportFile, true);
      }

      final DiffLoader diffLoader = new DiffLoader();
      diffLoader.loadDiff(diff, data);

      if (diff.getCallGraph(ESide.PRIMARY).getNodes().size() != 1) {
        throw new IllegalStateException(
            "Primary call graph of a single function diff has more than one vertex.");
      }
      if (diff.getCallGraph(ESide.SECONDARY).getNodes().size() != 1) {
        throw new IllegalStateException(
            "Secondary call graph of a single function diff has more than one vertex.");
      }

      final RawFunction priFunction = diff.getCallGraph(ESide.PRIMARY).getNodes().get(0);
      final RawFunction secFunction = diff.getCallGraph(ESide.SECONDARY).getNodes().get(0);

      priFunction.setSizeOfBasicBlocks(metaData.getSizeOfBasicBlocks(ESide.PRIMARY));
      secFunction.setSizeOfBasicBlocks(metaData.getSizeOfBasicBlocks(ESide.SECONDARY));
      priFunction.setSizeOfJumps(metaData.getSizeOfJumps(ESide.PRIMARY));
      secFunction.setSizeOfJumps(metaData.getSizeOfJumps(ESide.SECONDARY));
      priFunction.setSizeOfInstructions(metaData.getSizeOfInstructions(ESide.PRIMARY));
      secFunction.setSizeOfInstructions(metaData.getSizeOfInstructions(ESide.SECONDARY));

      final IAddress priFunctionAddr = priFunction.getAddress();
      final IAddress secFunctionAddr = secFunction.getAddress();

      metaData.setFunctionAddr(priFunctionAddr, ESide.PRIMARY);
      metaData.setFunctionAddr(secFunctionAddr, ESide.SECONDARY);
      metaData.setFunctionName(priFunction.getName(), ESide.PRIMARY);
      metaData.setFunctionName(secFunction.getName(), ESide.SECONDARY);
      metaData.setFunctionType(priFunction.getFunctionType(), ESide.PRIMARY);
      metaData.setFunctionType(secFunction.getFunctionType(), ESide.SECONDARY);

      final FunctionMatchData functionMatch =
          diff.getMatches().getFunctionMatch(priFunctionAddr, ESide.PRIMARY);
      matchesDB.loadBasicBlockMatches(functionMatch);

      CommentsDatabase commentsDatabase = null;

      if (workspace.isLoaded()) {
        commentsDatabase = new CommentsDatabase(workspace, true);
      }

      final RawFlowGraph priFlowGraph;
      priFlowGraph =
          DiffLoader.loadRawFlowGraph(commentsDatabase, diff, priFunctionAddr, ESide.PRIMARY);
      final RawFlowGraph secFlowGraph;
      secFlowGraph =
          DiffLoader.loadRawFlowGraph(commentsDatabase, diff, secFunctionAddr, ESide.SECONDARY);
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedFlowGraph;
      combinedFlowGraph =
          RawCombinedFlowGraphBuilder.buildRawCombinedFlowGraph(
              functionMatch, priFlowGraph, secFlowGraph);

      createSingleFunctionDiffFlowGraphView(
          diff, functionMatch, priFlowGraph, secFlowGraph, combinedFlowGraph);
    }
  }
}
