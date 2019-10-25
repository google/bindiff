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
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelInitializer;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.io.matches.FunctionDiffSocketXmlData;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.builders.RawCombinedFlowGraphBuilder;
import com.google.security.zynamics.bindiff.project.matches.FunctionDiffMetaData;
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

public class FunctionDiffViewLoader extends CEndlessHelperThread {
  private final FunctionDiffSocketXmlData data;
  private final MainWindow window;
  private final TabPanelManager tabPanelManager;
  private final Workspace workspace;

  private FlowGraphViewData viewData;

  public FunctionDiffViewLoader(
      final FunctionDiffSocketXmlData data,
      final MainWindow window,
      final TabPanelManager tabPanelManager,
      final Workspace workspace) {
    this.data = Preconditions.checkNotNull(data);
    this.window = Preconditions.checkNotNull(window);
    this.tabPanelManager = Preconditions.checkNotNull(tabPanelManager);
    this.workspace = Preconditions.checkNotNull(workspace);
    this.viewData = null;
  }

  private void createSingleFunctionDiffFlowgraphView(
      final Diff diff,
      final FunctionMatchData functionMatch,
      final RawFlowGraph priFlowgraph,
      final RawFlowGraph secFlowgraph,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedFlowgraph)
      throws GraphCreationException {
    final GraphsContainer graphs =
        ViewFlowGraphBuilder.buildViewFlowgraphs(diff, functionMatch, combinedFlowgraph);

    final RawFunction priFunction =
        diff.getFunction(functionMatch.getIAddress(ESide.PRIMARY), ESide.PRIMARY);
    final RawFunction secFunction =
        diff.getFunction(functionMatch.getIAddress(ESide.SECONDARY), ESide.SECONDARY);

    String name = diff.getMatchesDatabase().getName();
    name =
        BinDiffFileUtils.forceFilenameEndsNotWithExtension(
            name, Constants.BINDIFF_MATCHES_DB_EXTENSION);

    if (!workspace.isLoaded()
        || (workspace.isLoaded()
            && diff.getMatchesDatabase().getParent().indexOf(workspace.getWorkspaceDirPath())
                != 0)) {
      name = String.format("%s vs %s", priFunction.getName(), secFunction.getName());
    }

    viewData =
        new FlowGraphViewData(
            priFlowgraph,
            secFlowgraph,
            combinedFlowgraph,
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
      // FIXME: Never catch all exceptions!
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
      final FunctionDiffMetaData metaData = matchesDB.loadFunctionDiffMetaData(true);

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
            "Primary callgraph of a single function diff has more than one vertex.");
      }
      if (diff.getCallGraph(ESide.SECONDARY).getNodes().size() != 1) {
        throw new IllegalStateException(
            "Secondary callgraph of a single function diff has more than one vertex.");
      }

      final RawFunction priFunction = diff.getCallGraph(ESide.PRIMARY).getNodes().get(0);
      final RawFunction secFunction = diff.getCallGraph(ESide.SECONDARY).getNodes().get(0);

      priFunction.setSizeOfBasicBlocks(metaData.getSizeOfBasicblocks(ESide.PRIMARY));
      secFunction.setSizeOfBasicBlocks(metaData.getSizeOfBasicblocks(ESide.SECONDARY));
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

      final RawFlowGraph priFlowgraph;
      priFlowgraph =
          DiffLoader.loadRawFlowGraph(commentsDatabase, diff, priFunctionAddr, ESide.PRIMARY);
      final RawFlowGraph secFlowgraph;
      secFlowgraph =
          DiffLoader.loadRawFlowGraph(commentsDatabase, diff, secFunctionAddr, ESide.SECONDARY);
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedFlowgraph;
      combinedFlowgraph =
          RawCombinedFlowGraphBuilder.buildRawCombinedFlowgraph(
              functionMatch, priFlowgraph, secFlowgraph);

      createSingleFunctionDiffFlowgraphView(
          diff, functionMatch, priFlowgraph, secFlowgraph, combinedFlowgraph);
    }
  }
}
