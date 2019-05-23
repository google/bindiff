package com.google.security.zynamics.bindiff.project.diff;

import com.google.common.base.Preconditions;
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
    this.tabPanelManager = Preconditions.checkNotNull(tabPanelManager);
    this.window = Preconditions.checkNotNull(window);
    this.workspace = Preconditions.checkNotNull(workspace);
    this.viewsAddrs = Preconditions.checkNotNull(viewsAddrs);
  }

  private void createFlowgraphView(final FlowGraphViewData viewData) throws GraphCreationException {
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
      // FIXME: Never catch all exceptions!
      throw new GraphCreationException("An error occurred while initializing the graph.");
    }

    tabPanelManager.addTab(viewTabPanel);

    ViewTabPanelInitializer.centerSingleGraphs(viewData.getGraphs().getSuperGraph());
    ViewTabPanelInitializer.centerCombinedGraph(viewData.getGraphs(), viewTabPanel);

    tabPanelManager.selectTab(viewTabPanel);
  }

  private FlowGraphViewData loadFlowgraphViewData(
      final Diff diff, final IAddress priFunctionAddr, final IAddress secFunctionAddr)
      throws IOException, GraphCreationException, SQLException {
    // Load primary and secondary raw flowgraphs.
    try (final CommentsDatabase database = new CommentsDatabase(workspace, true)) {
      RawFlowGraph primaryRawFlowgraph = null;
      RawFlowGraph secondaryRawFlowgraph = null;
      final FunctionMatchData functionMatch =
          loadFunctionMatchData(diff, priFunctionAddr, secFunctionAddr);
      if (priFunctionAddr != null) {
        setDescription("Loading primary raw function data...");
        primaryRawFlowgraph =
            DiffLoader.loadRawFlowGraph(database, diff, priFunctionAddr, ESide.PRIMARY);
      }
      if (secFunctionAddr != null) {
        setDescription("Loading secondary raw function data...");
        secondaryRawFlowgraph =
            DiffLoader.loadRawFlowGraph(database, diff, secFunctionAddr, ESide.SECONDARY);
      }

      setDescription("Building combined flow graph...");
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedRawFlowgraph =
              RawCombinedFlowGraphBuilder.buildRawCombinedFlowgraph(
                  functionMatch, primaryRawFlowgraph, secondaryRawFlowgraph);

      setDescription("Creating flow graph view...");
      final GraphsContainer graphs =
          ViewFlowGraphBuilder.buildViewFlowgraphs(diff, functionMatch, combinedRawFlowgraph);

      final FlowGraphViewData view =
          new FlowGraphViewData(
              primaryRawFlowgraph,
              secondaryRawFlowgraph,
              combinedRawFlowgraph,
              graphs,
              FlowGraphViewData.getViewName(graphs),
              EViewType.FUNCTION_DIFF_VIEW);

      diff.getViewManager().addView(view);

      return view;
    }
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
          loadFlowgraphViewData(viewAddrs.first(), viewAddrs.second(), viewAddrs.third());
      createFlowgraphView(viewData);
    }
  }
}
