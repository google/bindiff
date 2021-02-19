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

import com.google.security.zynamics.bindiff.database.MatchesDatabase;
import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.EViewType;
import com.google.security.zynamics.bindiff.exceptions.GraphCreationException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.builders.ViewCallGraphBuilder;
import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;
import com.google.security.zynamics.bindiff.graph.searchers.GraphAddressSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelInitializer;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.io.matches.DiffRequestMessage;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.builders.RawCombinedCallGraphBuilder;
import com.google.security.zynamics.bindiff.project.matches.DiffMetadata;
import com.google.security.zynamics.bindiff.project.matches.FunctionDiffMetadata;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedCallGraph;
import com.google.security.zynamics.bindiff.project.userview.CallGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.io.File;

/** Loads call graphs into a new view. */
public class CallGraphViewLoader extends CEndlessHelperThread {
  private final DiffRequestMessage data;

  private final MainWindow window;
  private final TabPanelManager tabPanelManager;
  private final Workspace workspace;

  public CallGraphViewLoader(
      final Diff diff,
      final MainWindow window,
      final TabPanelManager tabPanelManager,
      final Workspace workspace) {
    this(new DiffRequestMessage(checkNotNull(diff)), window, tabPanelManager, workspace);
  }

  public CallGraphViewLoader(
      final DiffRequestMessage data,
      final MainWindow window,
      final TabPanelManager tabPanelManager,
      final Workspace workspace) {
    this.data = checkNotNull(data);
    this.window = checkNotNull(window);
    this.tabPanelManager = checkNotNull(tabPanelManager);
    this.workspace = checkNotNull(workspace);
  }

  private static void zoomToMatch(final IAddress primaryAddress, final BinDiffGraph<?, ?> graph) {
    ZyGraphNode<?> node = null;
    if (graph instanceof SingleGraph) {
      node = GraphAddressSearcher.searchAddress((SingleGraph) graph, primaryAddress);
    } else if (graph instanceof CombinedGraph) {
      node =
          GraphAddressSearcher.searchAddress((CombinedGraph) graph, ESide.PRIMARY, primaryAddress);
    }
    if (node != null) {
      GraphZoomer.zoomToNode(graph, node);
    }
  }

  private void createCallGraphView(final Diff diff, final CallGraphViewData view)
      throws GraphCreationException {
    final ViewTabPanel viewTabPanel =
        new ViewTabPanel(window, tabPanelManager, workspace, diff, view);

    tabPanelManager.addTab(viewTabPanel);

    setDescription("Creating view...");

    try {
      final GraphsContainer graphs = view.getGraphs();
      ViewTabPanelInitializer.initialize(graphs, this);

      tabPanelManager.selectTab(viewTabPanel);

      final EDiffViewMode mode = viewTabPanel.getController().getGraphSettings().getDiffViewMode();
      final BinDiffGraph<?, ?> graph =
          mode == EDiffViewMode.NORMAL_VIEW ? graphs.getPrimaryGraph() : graphs.getCombinedGraph();
      if (graph != null) {
        zoomToMatch(new CAddress(data.getFunctionAddress(ESide.PRIMARY)), graph);
      }
    } catch (final Exception e) {
      throw new GraphCreationException("An error occurred while initializing the graph.");
    }
  }

  private CallGraphViewData loadCallGraphViewData(final Diff diff) throws GraphCreationException {
    // Note: Primary and secondary raw call graphs are already loaded with the diff itself.
    final RawCallGraph primaryCallGraph = diff.getCallGraph(ESide.PRIMARY);
    final RawCallGraph secondaryCallGraph = diff.getCallGraph(ESide.SECONDARY);

    setDescription("Building raw call graph...");
    RawCombinedCallGraph combinedCallGraph =
        RawCombinedCallGraphBuilder.buildCombinedCallgraph(
            diff.getMatches(), primaryCallGraph, secondaryCallGraph);

    setDescription("Building view call graph...");
    final GraphsContainer graphs =
        ViewCallGraphBuilder.buildDiffCallgraphs(diff, combinedCallGraph);

    final DiffMetadata meta = diff.getMetadata();
    final CallGraphViewData view =
        new CallGraphViewData(
            primaryCallGraph,
            secondaryCallGraph,
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
    Diff diff = data.getDiff();
    if (diff == null) {
      final File matchesFile = new File(data.getMatchesDBPath());

      try (final MatchesDatabase matchesDB = new MatchesDatabase(matchesFile)) {
        final FunctionDiffMetadata metadata = matchesDB.loadFunctionDiffMetadata(true);
        final File primaryExportFile = new File(data.getBinExportPath(ESide.PRIMARY));
        final File secondaryExportFile = new File(data.getBinExportPath(ESide.SECONDARY));

        diff = new Diff(metadata, matchesFile, primaryExportFile, secondaryExportFile, false);
        final DiffLoader diffLoader = new DiffLoader();
        diffLoader.loadDiff(diff, data);
      }
    }
    final CallGraphViewData view = loadCallGraphViewData(diff);
    createCallGraphView(diff, view);
  }
}
