package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.database.MatchesDatabase;
import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.enums.EGraphLayout;
import com.google.security.zynamics.bindiff.enums.EGraphSynchronization;
import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.builders.ViewCallGraphBuilder;
import com.google.security.zynamics.bindiff.graph.builders.ViewFlowGraphBuilder;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.bindiff.graph.eventhandlers.GraphLayoutEventHandler;
import com.google.security.zynamics.bindiff.graph.helpers.BasicBlockMatchAdder;
import com.google.security.zynamics.bindiff.graph.helpers.BasicBlockMatchRemover;
import com.google.security.zynamics.bindiff.graph.helpers.GraphColorizer;
import com.google.security.zynamics.bindiff.graph.helpers.GraphSelector;
import com.google.security.zynamics.bindiff.graph.helpers.GraphViewFitter;
import com.google.security.zynamics.bindiff.graph.helpers.GraphZoomer;
import com.google.security.zynamics.bindiff.graph.listeners.GraphViewsListenerManager;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.settings.GraphProximityBrowsingSettings;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.dialogs.ExportViewDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.ProgressDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.SaveFunctionDiffViewDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.CriteriaDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.CriteriaFactory;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.CriteriumExecuter;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriteriumTree;
import com.google.security.zynamics.bindiff.gui.dialogs.graphsettings.GraphSettingsDialog;
import com.google.security.zynamics.bindiff.gui.dialogs.printing.PrintGraphPreviewDialog;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.GraphPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels.ViewToolbarPanel;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.io.CommentsWriter;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.FunctionDiffViewSaver;
import com.google.security.zynamics.bindiff.project.diff.IDiffListener;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.matches.DiffMetaData;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawJump;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.CColorChooser;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.zygraph.CDefaultLabelEventHandler;
import com.google.security.zynamics.zylib.gui.zygraph.ILabelEditableContentListener;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.GraphExporters;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ViewTabPanelFunctions extends TabPanelFunctions {
  private final ListenerProvider<ISavableListener> listenerProvider = new ListenerProvider<>();

  private final InternalEditableContentListener labelEditModeListener =
      new InternalEditableContentListener();

  private final GraphsContainer graphs;
  private final GraphSettings settings;
  private final GraphViewsListenerManager graphListenerManager;

  private ViewTabPanel viewTabPanel;

  private Color currentColor = null;
  private CriteriaDialog selectByCriteriaDlg = null;
  private GraphSettingsDialog settingsDialog = null;

  // Indicates whether the use made save-able changes (which haven't been saved yet)
  private boolean hasChangedMatches = false;
  private boolean hasChangedComments = false;

  public ViewTabPanelFunctions(
      MainWindow window,
      final Workspace workspace,
      final ViewTabPanel tabPanel,
      final ViewData view) {
    super(window, workspace);

    viewTabPanel = Preconditions.checkNotNull(tabPanel);
    Preconditions.checkNotNull(view);

    graphs = view.getGraphs();
    settings = graphs.getSettings();

    graphListenerManager = new GraphViewsListenerManager(graphs, this);

    graphs
        .getCombinedGraph()
        .getEditMode()
        .getLabelEventHandler()
        .addEditModeListener(labelEditModeListener);
    graphs
        .getPrimaryGraph()
        .getEditMode()
        .getLabelEventHandler()
        .addEditModeListener(labelEditModeListener);
    graphs
        .getSecondaryGraph()
        .getEditMode()
        .getLabelEventHandler()
        .addEditModeListener(labelEditModeListener);
  }

  public static boolean isNodeSelectionMatchAddable(
      final BinDiffGraph<?, ?> graph, final ZyGraphNode<?> clickedNode) {
    return BasicBlockMatchAdder.getAffectedCombinedNodes(graph, clickedNode) != null;
  }

  public static boolean isNodeSelectionMatchDeleteable(
      final BinDiffGraph<?, ?> graph, final ZyGraphNode<?> clickedNode) {
    return BasicBlockMatchRemover.getAffectedCombinedNodes(graph, clickedNode) != null;
  }

  private void notifySaveableListener() {
    for (final ISavableListener listener : listenerProvider) {
      listener.isSavable(hasChanged());
    }
  }

  private void setCommentsChanged(final boolean changed) {
    hasChangedComments = changed;
    notifySaveableListener();
  }

  private void setMatchesChanged(final boolean changed) {
    hasChangedMatches = changed;
    notifySaveableListener();
  }

  private boolean showColorChooserDialog() {
    final List<Color> defaultColors = GraphColorizer.getRecentColors();

    currentColor =
        CColorChooser.showDialog(
            viewTabPanel, "Color Nodes", Color.WHITE, defaultColors.toArray(new Color[0]));
    if (currentColor != null) {
      defaultColors.add(currentColor);
      GraphColorizer.setRecentColors(defaultColors);
      return true;
    }

    return false;
  }

  private void updateFunctionMatchCounts() {
    final ViewData viewData = viewTabPanel.getView();

    if (viewData.isFlowgraphView() || viewData.isSingleFunctionDiffView()) {
      final Diff diff = viewData.getGraphs().getDiff();
      final IAddress priAddr = viewData.getAddress(ESide.PRIMARY);
      final FunctionMatchData functionMatch =
          diff.getMatches().getFunctionMatch(priAddr, ESide.PRIMARY);

      @SuppressWarnings("unchecked")
      final RawFlowGraph priFlowgraph = (RawFlowGraph) viewData.getRawGraph(ESide.PRIMARY);

      int basicblockCounter = 0;
      int jumpCounter = 0;
      int instructionCounter = 0;
      for (final RawBasicBlock basicblock : priFlowgraph.getNodes()) {
        if (basicblock.getMatchState() == EMatchState.MATCHED) {
          ++basicblockCounter;

          for (final SingleViewEdge<? extends SingleViewNode> edge :
              basicblock.getOutgoingEdges()) {
            @SuppressWarnings("unchecked")
            final RawJump jump = (RawJump) edge;
            if (jump.getSource().getMatchState() == EMatchState.MATCHED
                && jump.getTarget().getMatchState() == EMatchState.MATCHED) {
              ++jumpCounter;
            }
          }

          final IAddress priBasicblockAddr = basicblock.getAddress();
          final BasicBlockMatchData basicblockMatch =
              functionMatch.getBasicblockMatch(priBasicblockAddr, ESide.PRIMARY);
          instructionCounter += basicblockMatch.getSizeOfMatchedInstructions();
        }
      }

      functionMatch.setSizeOfMatchedBasicblocks(basicblockCounter);
      functionMatch.setSizeOfMatchedJumps(jumpCounter);
      functionMatch.setSizeOfMatchedInstructions(instructionCounter);
    }
  }

  public void addListener(final ISavableListener viewListener) {
    listenerProvider.addListener(viewListener);
  }

  public void addNodeMatch(
      final CombinedDiffNode oldPriUnmatchedCombinedDiffNode,
      final CombinedDiffNode oldSecUnmatchedCombinedDiffNode) {
    if (oldPriUnmatchedCombinedDiffNode == null || oldSecUnmatchedCombinedDiffNode == null) {
      return;
    }

    if (viewTabPanel.getView().isFlowgraphView()) {
      try {
        BasicBlockMatchAdder.addBasicblockMatch(
            graphs, oldPriUnmatchedCombinedDiffNode, oldSecUnmatchedCombinedDiffNode);
        setMatchesChanged(true);
      } catch (final GraphLayoutException e) {
        Logger.logException(e, e.getMessage());
        CMessageBox.showError(viewTabPanel, e.getMessage());
      }
    }
  }

  public boolean closeView(final boolean save) {
    final ViewData viewData = viewTabPanel.getView();

    if (save && hasChanged()) {
      if (!saveView()) {
        return false;
      }
    }

    final Diff diff = viewTabPanel.getDiff();
    diff.getViewManager().removeView(viewData);
    getMainWindow().getController().getTabPanelManager().removeTab(viewTabPanel);

    viewTabPanel.dispose();
    dispose();
    graphs.dispose();
    viewTabPanel = null;

    if (viewData.isCallgraphView()) {
      diff.getCallgraph(ESide.PRIMARY).resetVisibilityAndSelection();
      diff.getCallgraph(ESide.SECONDARY).resetVisibilityAndSelection();
    }

    for (final IDiffListener diffListener : diff.getListener()) {
      diffListener.closedView(diff);
    }

    return true;
  }

  public void colorInvisibleNodes() {
    if (showColorChooserDialog()) {
      final Color color = currentColor;

      if (settings.isSync()) {
        GraphColorizer.colorizeInvisibleNodes(graphs.getCombinedGraph(), color);
        GraphColorizer.colorizeInvisibleNodes(graphs.getPrimaryGraph(), color);
        GraphColorizer.colorizeInvisibleNodes(graphs.getSecondaryGraph(), color);
      } else {
        switch (settings.getDiffViewMode()) {
          case NORMAL_VIEW:
            {
              if (settings.getFocus() == ESide.PRIMARY) {
                GraphColorizer.colorizeInvisibleNodes(graphs.getPrimaryGraph(), color);
              } else {
                GraphColorizer.colorizeInvisibleNodes(graphs.getSecondaryGraph(), color);
              }
              break;
            }
          case COMBINED_VIEW:
            {
              GraphColorizer.colorizeInvisibleNodes(graphs.getCombinedGraph(), color);
              break;
            }
          default:
        }
      }
    }
  }

  public void colorSelectedNodes() {
    if (showColorChooserDialog()) {
      final Color color = currentColor;

      if (settings.isSync()) {
        GraphColorizer.colorizeSelectedNodes(graphs.getCombinedGraph(), color);
        GraphColorizer.colorizeSelectedNodes(graphs.getPrimaryGraph(), color);
        GraphColorizer.colorizeSelectedNodes(graphs.getSecondaryGraph(), color);
      } else {
        switch (settings.getDiffViewMode()) {
          case NORMAL_VIEW:
            {
              if (settings.getFocus() == ESide.PRIMARY) {
                GraphColorizer.colorizeSelectedNodes(graphs.getPrimaryGraph(), color);
              } else {
                GraphColorizer.colorizeSelectedNodes(graphs.getSecondaryGraph(), color);
              }
              break;
            }
          case COMBINED_VIEW:
            {
              GraphColorizer.colorizeSelectedNodes(graphs.getCombinedGraph(), color);
              break;
            }
          default:
        }
      }
    }
  }

  public void colorUnslectedNodes() {
    if (showColorChooserDialog()) {
      final Color color = currentColor;

      if (settings.isSync()) {
        GraphColorizer.colorizeUnselectedNodes(graphs.getCombinedGraph(), color);
        GraphColorizer.colorizeUnselectedNodes(graphs.getPrimaryGraph(), color);
        GraphColorizer.colorizeUnselectedNodes(graphs.getSecondaryGraph(), color);
      } else {
        switch (settings.getDiffViewMode()) {
          case NORMAL_VIEW:
            {
              if (settings.getFocus() == ESide.PRIMARY) {
                GraphColorizer.colorizeUnselectedNodes(graphs.getPrimaryGraph(), color);
              } else {
                GraphColorizer.colorizeUnselectedNodes(graphs.getSecondaryGraph(), color);
              }
              break;
            }
          case COMBINED_VIEW:
            {
              GraphColorizer.colorizeUnselectedNodes(graphs.getCombinedGraph(), color);
              break;
            }
          default:
        }
      }
    }
  }

  public void deselectLeafs() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW && !settings.isSync()) {
      if (settings.getFocus() == ESide.PRIMARY) {
        GraphSelector.deselectLeafs(graphs.getPrimaryGraph());
      } else {
        GraphSelector.deselectLeafs(graphs.getSecondaryGraph());
      }
    } else {
      GraphSelector.deselectLeafs(graphs.getCombinedGraph());
    }
  }

  public void deselectPeriphery() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW && !settings.isSync()) {
      if (settings.getFocus() == ESide.PRIMARY) {
        GraphSelector.deselectPeriphery(graphs.getPrimaryGraph());
      } else {
        GraphSelector.deselectPeriphery(graphs.getSecondaryGraph());
      }
    } else {
      GraphSelector.deselectPeriphery(graphs.getCombinedGraph());
    }
  }

  public void deselectRoots() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW && !settings.isSync()) {
      if (settings.getFocus() == ESide.PRIMARY) {
        GraphSelector.deselectRoots(graphs.getPrimaryGraph());
      } else {
        GraphSelector.deselectRoots(graphs.getSecondaryGraph());
      }
    } else {
      GraphSelector.deselectRoots(graphs.getCombinedGraph());
    }
  }

  public void dispose() {
    graphListenerManager.dispose();

    final CDefaultLabelEventHandler primaryHandler =
        graphs.getPrimaryGraph().getEditMode().getLabelEventHandler();
    final CDefaultLabelEventHandler secondaryHandler =
        graphs.getSecondaryGraph().getEditMode().getLabelEventHandler();
    final CDefaultLabelEventHandler combinedHandler =
        graphs.getCombinedGraph().getEditMode().getLabelEventHandler();

    if (combinedHandler != null) {
      combinedHandler.removeEditModeListener(labelEditModeListener);
    }
    if (primaryHandler != null) {
      primaryHandler.removeEditModeListener(labelEditModeListener);
    }
    if (secondaryHandler != null) {
      secondaryHandler.removeEditModeListener(labelEditModeListener);
    }

    if (selectByCriteriaDlg != null) {
      selectByCriteriaDlg.dispose();
      selectByCriteriaDlg = null;
    }
  }

  public void doLayout(final EGraphLayout layoutStyle) {
    BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph =
        graphs.getCombinedGraph();

    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      if (settings.getFocus() == ESide.PRIMARY) {
        graph = graphs.getPrimaryGraph();
      } else {
        graph = graphs.getSecondaryGraph();
      }
    }

    if (graph != null) {
      switch (layoutStyle) {
        case CIRCULAR:
          graph.getSettings().getLayoutSettings().setDefaultGraphLayout(EGraphLayout.CIRCULAR);
          break;
        case HIERARCHICAL:
          graph.getSettings().getLayoutSettings().setDefaultGraphLayout(EGraphLayout.HIERARCHICAL);
          break;
        case ORTHOGONAL:
          graph.getSettings().getLayoutSettings().setDefaultGraphLayout(EGraphLayout.ORTHOGONAL);
          break;
      }

      graphListenerManager.suppressUpdating(true);

      GraphLayoutEventHandler.handleDoLayoutButtonEvent(graph, true);

      graphListenerManager.suppressUpdating(false);
    }
  }

  public void exportViewAsImage() {
    final MainWindow parent = getMainWindow();

    final ExportViewDialog viewChooserDlg;
    viewChooserDlg =
        new ExportViewDialog(
            parent,
            "Export View as Image",
            "",
            new File(graphs.getDiff().getDiffFolder()),
            viewTabPanel.getView().getViewName().toString());
    viewChooserDlg.setVisible(true);

    if (viewChooserDlg.isOkPressed()) {
      try {
        if (viewChooserDlg.isCapturePart()) {
          if (viewChooserDlg.isPNG()) {
            GraphExporters.exportPartAsPNG(
                getGraphs().getPrimaryGraph(), viewChooserDlg.getPrimaryImageFile().getPath());
            GraphExporters.exportPartAsPNG(
                getGraphs().getSecondaryGraph(), viewChooserDlg.getSecondaryImageFile().getPath());
            GraphExporters.exportPartAsPNG(
                getGraphs().getCombinedGraph(), viewChooserDlg.getCombinedImageFile().getPath());
          } else if (viewChooserDlg.isJPEG()) {
            GraphExporters.exportPartAsJPEG(
                getGraphs().getPrimaryGraph(), viewChooserDlg.getPrimaryImageFile().getPath());
            GraphExporters.exportPartAsJPEG(
                getGraphs().getSecondaryGraph(), viewChooserDlg.getSecondaryImageFile().getPath());
            GraphExporters.exportPartAsJPEG(
                getGraphs().getCombinedGraph(), viewChooserDlg.getCombinedImageFile().getPath());
          } else if (viewChooserDlg.isGIF()) {

            GraphExporters.exportPartAsGIF(
                getGraphs().getPrimaryGraph(), viewChooserDlg.getPrimaryImageFile().getPath());
            GraphExporters.exportPartAsGIF(
                getGraphs().getSecondaryGraph(), viewChooserDlg.getSecondaryImageFile().getPath());
            GraphExporters.exportPartAsGIF(
                getGraphs().getCombinedGraph(), viewChooserDlg.getCombinedImageFile().getPath());
          } else if (viewChooserDlg.isSVG()) {
            GraphExporters.exportPartAsSVG(
                getGraphs().getPrimaryGraph(), viewChooserDlg.getPrimaryImageFile().getPath());
            GraphExporters.exportPartAsSVG(
                getGraphs().getSecondaryGraph(), viewChooserDlg.getSecondaryImageFile().getPath());
            GraphExporters.exportPartAsSVG(
                getGraphs().getCombinedGraph(), viewChooserDlg.getCombinedImageFile().getPath());
          }
        } else {
          if (viewChooserDlg.isPNG()) {
            GraphExporters.exportAllAsPNG(
                getGraphs().getPrimaryGraph(), viewChooserDlg.getPrimaryImageFile().getPath());
            GraphExporters.exportAllAsPNG(
                getGraphs().getSecondaryGraph(), viewChooserDlg.getSecondaryImageFile().getPath());
            GraphExporters.exportAllAsPNG(
                getGraphs().getCombinedGraph(), viewChooserDlg.getCombinedImageFile().getPath());
          } else if (viewChooserDlg.isJPEG()) {
            GraphExporters.exportAllAsJPEG(
                getGraphs().getPrimaryGraph(), viewChooserDlg.getPrimaryImageFile().getPath());
            GraphExporters.exportAllAsJPEG(
                getGraphs().getSecondaryGraph(), viewChooserDlg.getSecondaryImageFile().getPath());
            GraphExporters.exportAllAsJPEG(
                getGraphs().getCombinedGraph(), viewChooserDlg.getCombinedImageFile().getPath());
          } else if (viewChooserDlg.isGIF()) {

            GraphExporters.exportAllAsGIF(
                getGraphs().getPrimaryGraph(), viewChooserDlg.getPrimaryImageFile().getPath());
            GraphExporters.exportAllAsGIF(
                getGraphs().getSecondaryGraph(), viewChooserDlg.getSecondaryImageFile().getPath());
            GraphExporters.exportAllAsGIF(
                getGraphs().getCombinedGraph(), viewChooserDlg.getCombinedImageFile().getPath());
          } else if (viewChooserDlg.isSVG()) {
            GraphExporters.exportAllAsSVG(
                getGraphs().getPrimaryGraph(), viewChooserDlg.getPrimaryImageFile().getPath());
            GraphExporters.exportAllAsSVG(
                getGraphs().getSecondaryGraph(), viewChooserDlg.getSecondaryImageFile().getPath());
            GraphExporters.exportAllAsSVG(
                getGraphs().getCombinedGraph(), viewChooserDlg.getCombinedImageFile().getPath());
          }
        }
      } catch (final Exception e) {
        // FIXME: Never catch all exceptions!
        Logger.logException(e, "Couldn't save exported view images.");
        CMessageBox.showError(parent, "Couldn't save exported view images.");
      }
    }
  }

  public void fitGraphContentToView() {
    if (settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
      GraphZoomer.fitContent(graphs.getCombinedGraph());
    } else if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      GraphZoomer.fitContent(graphs.getSuperGraph());
    }
  }

  public void fitGraphs() {
    final Dimension combinedDim = viewTabPanel.getNormalViewPanel().getSize();
    final long w =
        Math.round(combinedDim.width * (1 - GraphPanel.COMBINED_MAIN_DIVIDER_WIDTH - 0.06));
    // 0.06 is an ugly offset/magic number (no clue why this offset is necessary, but seems to
    // work).
    final long h = Math.round(combinedDim.getHeight());
    graphs.getCombinedGraph().getView().setSize((int) w, (int) h);

    GraphViewFitter.fitSingleViewToSuperViewContent(graphs.getSuperGraph());

    graphs.getCombinedGraph().getGraph().fitGraph2DView();
    graphs
        .getCombinedGraph()
        .getView()
        .setZoom(graphs.getCombinedGraph().getView().getZoom() * 0.9);

    graphs.getCombinedGraph().getGraph().updateViews();
    graphs.getPrimaryGraph().getGraph().updateViews();
    graphs.getSecondaryGraph().getGraph().updateViews();
  }

  public JPanel getCurrentViewPanel() {
    switch (settings.getDiffViewMode()) {
      case NORMAL_VIEW:
        return viewTabPanel.getNormalViewPanel();
      case COMBINED_VIEW:
        return viewTabPanel.getCombinedViewPanel();
      default:
    }

    throw new IllegalStateException("Unknown view mode.");
  }

  public GraphViewsListenerManager getGraphListenerManager() {
    return graphListenerManager;
  }

  public GraphsContainer getGraphs() {
    return graphs;
  }

  public GraphSettings getGraphSettings() {
    return settings;
  }

  public TabPanelManager getTabPanelManager() {
    return viewTabPanel.getTabPanelManager();
  }

  public ViewTabPanel getViewTabPanel() {
    return viewTabPanel;
  }

  public boolean hasChanged() {
    return hasChangedMatches || hasChangedComments;
  }

  public void invertSelection() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW && !settings.isSync()) {
      if (settings.getFocus() == ESide.PRIMARY) {
        GraphSelector.invertSelection(graphs.getPrimaryGraph());
      } else {
        GraphSelector.invertSelection(graphs.getSecondaryGraph());
      }
    } else {
      GraphSelector.invertSelection(graphs.getCombinedGraph());
    }
  }

  public void openFlowgraphsViews(final ZyGraphNode<?> node) {
    final WorkspaceTabPanel workspaceTabPanel =
        viewTabPanel.getTabPanelManager().getWorkspaceTabPanel();
    final WorkspaceTabPanelFunctions workspaceController = workspaceTabPanel.getController();
    final Diff diff = getGraphs().getDiff();

    final IAddress priAddr;
    final IAddress secAddr;
    if (node instanceof SingleDiffNode) {
      final RawFunction rawFunction = (RawFunction) node.getRawNode();
      if (rawFunction.getSide() == ESide.PRIMARY) {
        final RawFunction matchedFunction = rawFunction.getMatchedFunction();
        priAddr = rawFunction.getAddress();
        secAddr = matchedFunction == null ? null : matchedFunction.getAddress();
      } else {
        final RawFunction matchedFunction = rawFunction.getMatchedFunction();
        priAddr = matchedFunction == null ? null : matchedFunction.getAddress();
        secAddr = rawFunction.getAddress();
      }
    } else if (node instanceof CombinedDiffNode) {
      final RawCombinedFunction rawCombinedFunction = (RawCombinedFunction) node.getRawNode();
      priAddr = rawCombinedFunction.getAddress(ESide.PRIMARY);
      secAddr = rawCombinedFunction.getAddress(ESide.SECONDARY);
    } else {
      return;
    }

    workspaceController.openFlowgraphView(getMainWindow(), diff, priAddr, secAddr);
  }

  public void printView(final BinDiffGraph<?, ?> graph) {
    new PrintGraphPreviewDialog(getMainWindow(), graph.getView());
  }

  public void redoSelection() {
    graphs.getPrimaryGraph().getSelectionHistory().setEnabled(false);
    graphs.getSecondaryGraph().getSelectionHistory().setEnabled(false);
    graphs.getCombinedGraph().getSelectionHistory().setEnabled(false);

    graphs.getPrimaryGraph().getSelectionHistory().redo();
    graphs.getSecondaryGraph().getSelectionHistory().redo();
    graphs.getCombinedGraph().getSelectionHistory().redo();

    graphs.getPrimaryGraph().getSelectionHistory().setEnabled(true);
    graphs.getSecondaryGraph().getSelectionHistory().setEnabled(true);
    graphs.getCombinedGraph().getSelectionHistory().setEnabled(true);
  }

  public void removeListener(final ISavableListener viewListener) {
    listenerProvider.removeListener(viewListener);
  }

  public void removeNodeMatch(final List<CombinedDiffNode> nodes) {
    if (nodes == null) {
      return;
    }

    if (viewTabPanel.getView().isFlowgraphView()) {
      try {
        for (final CombinedDiffNode combinedNode : nodes) {
          BasicBlockMatchRemover.removeBasicblockMatch(graphs, combinedNode);
          setMatchesChanged(true);
        }
      } catch (final GraphLayoutException e) {
        Logger.logException(e, e.getMessage());
        CMessageBox.showError(viewTabPanel, e.getMessage());
      } catch (final Exception e) {
        // TODO(cblichmann): Never catch all exceptions!
        Logger.logException(e, e.getMessage());
        CMessageBox.showError(viewTabPanel, e.getMessage());
      }
    }
  }

  public void resetDefaultPerspective() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      viewTabPanel.getNormalViewPanel().resetDefaultPerspective();
    } else if (settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
      viewTabPanel.getCombinedViewPanel().resetDefaultPerspective();
    }
  }

  public boolean saveView() {
    final Diff diff = graphs.getDiff();

    if (diff.isFunctionDiff()) {
      try {
        final SaveFunctionDiffViewDialog dlg =
            new SaveFunctionDiffViewDialog(
                getMainWindow(), "Save Function Diff View", getWorkspace(), diff);
        dlg.setVisible(true);

        if (dlg.isOkPressed()) {
          updateFunctionMatchCounts();
          final FunctionDiffViewSaver saver =
              new FunctionDiffViewSaver(
                  this,
                  dlg.getExportBinaryTargetFile(ESide.PRIMARY),
                  dlg.getExportBinaryTargetFile(ESide.SECONDARY),
                  dlg.getMatchesDatabaseTargetFile(),
                  dlg.isOverrideExportBinary(ESide.PRIMARY),
                  dlg.isOverrideExportBinary(ESide.SECONDARY));

          ProgressDialog.show(getMainWindow(), "Saving View...", saver);

          final TabPanelManager viewManager = viewTabPanel.getTabPanelManager();
          viewManager.updateSelectedTabTitel(dlg.getFunctionDiffName());
          viewManager.udpateSelectedTabIcon();

          getMainWindow().updateTitle(getWorkspace(), viewTabPanel);
        } else {
          return false;
        }
      } catch (final Exception e) {
        // FIXME: Never catch all exceptions!
        Logger.logException(e, "Save function diff view failed.");
        CMessageBox.showError(getMainWindow(), "Save function diff view failed.");

        return false;
      }
    } else {
      // Normal diff view, don't change call sequence - updateFunctionMatchCounts must be called
      // first1.
      updateFunctionMatchCounts();
      writeComments();
      writeFlowgraphMatches();
    }

    setMatchesChanged(false);
    setCommentsChanged(false);

    return true;
  }

  public void selectAncestors() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW && !settings.isSync()) {
      if (settings.getFocus() == ESide.PRIMARY) {
        GraphSelector.selectAncestorsOfSelection(graphs.getPrimaryGraph());
      } else {
        GraphSelector.selectAncestorsOfSelection(graphs.getSecondaryGraph());
      }
    } else {
      GraphSelector.selectAncestorsOfSelection(graphs.getCombinedGraph());
    }
  }

  public void selectByCriteria() {
    if (selectByCriteriaDlg == null) {
      final CriteriaFactory factory = new CriteriaFactory(graphs);

      selectByCriteriaDlg =
          new CriteriaDialog(SwingUtilities.getWindowAncestor(viewTabPanel), factory);
    }

    selectByCriteriaDlg.setVisible(true);
    selectByCriteriaDlg.setVisible(false);

    final Set<ZyGraphNode<? extends CViewNode<?>>> nodesToSelect = new HashSet<>();

    final CriteriumTree tree = selectByCriteriaDlg.getCriteriumTree();

    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      if (settings.isSync()) {
        final Collection<CombinedDiffNode> toSelectCombined = new ArrayList<>();
        final Collection<CombinedDiffNode> toUnselectCombined = new ArrayList<>();

        final Collection<SuperDiffNode> toSelectSuper = new ArrayList<>();
        final Collection<SuperDiffNode> toUnselectSuper = new ArrayList<>();

        final Collection<SingleDiffNode> toSelectPrimarySingle = new ArrayList<>();
        final Collection<SingleDiffNode> toUnselectPrimarySingle = new ArrayList<>();
        final Collection<SingleDiffNode> toSelectSecondarySingle = new ArrayList<>();
        final Collection<SingleDiffNode> toUnselectSecondarySingle = new ArrayList<>();

        if (selectByCriteriaDlg.doSelectNodes()) {
          nodesToSelect.addAll(CriteriumExecuter.execute(tree, graphs.getPrimaryGraph()));
          nodesToSelect.addAll(CriteriumExecuter.execute(tree, graphs.getSecondaryGraph()));

          for (final SuperDiffNode superNode : graphs.getSuperGraph().getNodes()) {
            final SingleDiffNode primaryNode = superNode.getPrimaryDiffNode();
            final SingleDiffNode secondaryNode = superNode.getSecondaryDiffNode();
            final CombinedDiffNode combinedNode = superNode.getCombinedDiffNode();

            final boolean select =
                (primaryNode != null && nodesToSelect.contains(primaryNode))
                    || (secondaryNode != null && nodesToSelect.contains(secondaryNode));

            if (select) {
              if (primaryNode != null) {
                toSelectPrimarySingle.add(primaryNode);
              }

              if (secondaryNode != null) {
                toSelectSecondarySingle.add(secondaryNode);
              }

              toSelectCombined.add(combinedNode);
              toSelectSuper.add(superNode);
            } else {
              if (primaryNode != null) {
                toUnselectPrimarySingle.add(primaryNode);
              }

              if (secondaryNode != null) {
                toUnselectSecondarySingle.add(secondaryNode);
              }

              toUnselectCombined.add(combinedNode);
              toUnselectSuper.add(superNode);
            }
          }

          final boolean originalAutoLayout = settings.getLayoutSettings().getAutomaticLayouting();

          settings.getLayoutSettings().setAutomaticLayouting(false);

          graphs.getPrimaryGraph().selectNodes(toSelectPrimarySingle, toUnselectPrimarySingle);
          graphs
              .getSecondaryGraph()
              .selectNodes(toSelectSecondarySingle, toUnselectSecondarySingle);
          graphs.getCombinedGraph().selectNodes(toSelectCombined, toUnselectCombined);
          graphs.getSuperGraph().selectNodes(toSelectSuper, toUnselectSuper);

          settings.getLayoutSettings().setAutomaticLayouting(originalAutoLayout);
        }
      } else {
        if (settings.getFocus() == ESide.PRIMARY) {
          if (selectByCriteriaDlg.doSelectNodes()) {
            nodesToSelect.addAll(CriteriumExecuter.execute(tree, graphs.getPrimaryGraph()));
          }
        } else {
          if (selectByCriteriaDlg.doSelectNodes()) {
            nodesToSelect.addAll(CriteriumExecuter.execute(tree, graphs.getSecondaryGraph()));
          }
        }
      }
    } else if (settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
      if (selectByCriteriaDlg.doSelectNodes()) {
        nodesToSelect.addAll(CriteriumExecuter.execute(tree, graphs.getCombinedGraph()));
      }

      final Collection<CombinedDiffNode> toSelectCombined = new ArrayList<>();
      final Collection<CombinedDiffNode> toUnselectCombined = new ArrayList<>();

      for (final SuperDiffNode superNode : graphs.getSuperGraph().getNodes()) {

        final CombinedDiffNode combinedNode = superNode.getCombinedDiffNode();

        if (nodesToSelect.contains(combinedNode)) {
          toSelectCombined.add(combinedNode);
        } else {
          toUnselectCombined.add(combinedNode);
        }
      }

      graphs.getCombinedGraph().selectNodes(toSelectCombined, toUnselectCombined);
    }
  }

  public void selectChildren() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW && !settings.isSync()) {
      if (settings.getFocus() == ESide.PRIMARY) {
        GraphSelector.selectChildrenOfSelection(graphs.getPrimaryGraph());
      } else {
        GraphSelector.selectChildrenOfSelection(graphs.getSecondaryGraph());
      }
    } else {
      GraphSelector.selectChildrenOfSelection(graphs.getCombinedGraph());
    }
  }

  public void selectNeighbours() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW && !settings.isSync()) {
      if (settings.getFocus() == ESide.PRIMARY) {
        GraphSelector.selectNeighboursOfSelection(graphs.getPrimaryGraph());
      } else {
        GraphSelector.selectNeighboursOfSelection(graphs.getSecondaryGraph());
      }
    } else {
      GraphSelector.selectNeighboursOfSelection(graphs.getCombinedGraph());
    }
  }

  public void selectParents() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW && !settings.isSync()) {

      if (settings.getFocus() == ESide.PRIMARY) {
        GraphSelector.selectParentsOfSelection(graphs.getPrimaryGraph());
      } else {
        GraphSelector.selectParentsOfSelection(graphs.getSecondaryGraph());
      }
    } else {
      GraphSelector.selectParentsOfSelection(graphs.getCombinedGraph());
    }
  }

  public void selectSuccessors() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW && !settings.isSync()) {

      if (settings.getFocus() == ESide.PRIMARY) {
        GraphSelector.selectSuccessorsOfSelection(graphs.getPrimaryGraph());
      } else {
        GraphSelector.selectSuccessorsOfSelection(graphs.getSecondaryGraph());
      }
    } else {
      GraphSelector.selectSuccessorsOfSelection(graphs.getCombinedGraph());
    }
  }

  public void setCaretIntoJumpToAddressField(final ESide side) {
    viewTabPanel.getToolbar().setCaretIntoJumpToAddressField(side);
  }

  public void setCaretIntoSearchField() {
    viewTabPanel.getToolbar().setCaretIntoSearchField();
  }

  public void setDefaultNodeColors() {
    final Diff diff = graphs.getDiff();
    final CombinedGraph combinedGraph = graphs.getCombinedGraph();
    if (combinedGraph.getGraphType() == EGraphType.CALLGRAPH) {
      for (final CombinedDiffNode diffNode : combinedGraph.getNodes()) {
        ViewCallGraphBuilder.colorizeFunctions((RawCombinedFunction) diffNode.getRawNode());
      }
    } else {
      for (final CombinedDiffNode diffNode : combinedGraph.getNodes()) {
        FunctionMatchData match = null;
        final RawCombinedBasicBlock rawNode = (RawCombinedBasicBlock) diffNode.getRawNode();

        final IAddress priAddr = rawNode.getPrimaryFunctionAddress();
        match = diff.getMatches().getFunctionMatch(priAddr, ESide.PRIMARY);

        ViewFlowGraphBuilder.colorizeBasicblocks(match, rawNode);
      }
    }
  }

  public void setViewFocus(final ESide side) {
    settings.setFocusSide(side);
  }

  public void showGraphSettingDialog() {
    if (settingsDialog == null) {
      settingsDialog = new GraphSettingsDialog(getMainWindow(), getGraphSettings());
    }
    settingsDialog.setVisible(true);
  }

  public void showSearchResultsDialog() {
    final ViewToolbarPanel toolbar = viewTabPanel.getToolbar();
    toolbar.getSearchResultsDialog().setVisible(true);
  }

  public void switchViewPanel(final EDiffViewMode mode) {
    final JPanel oldView = getCurrentViewPanel();
    viewTabPanel.remove(oldView);

    settings.setDiffViewMode(mode);

    viewTabPanel.add(getCurrentViewPanel(), BorderLayout.CENTER);

    viewTabPanel.updateUI();
  }

  public void toggleAutoamticLayout() {
    settings
        .getLayoutSettings()
        .setAutomaticLayouting(!settings.getLayoutSettings().getAutomaticLayouting());
  }

  public void toggleGraphsPerspective() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      viewTabPanel.getNormalViewPanel().toggleGraphsPerspective();
    } else if (settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
      viewTabPanel.getCombinedViewPanel().toggleGraphsPerspective();
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void toggleGraphSynchronization() {
    if (EGraphSynchronization.SYNC == settings.getGraphSyncMode()) {
      settings.setGraphSyncMode(EGraphSynchronization.ASYNC);
    } else {
      settings.setGraphSyncMode(EGraphSynchronization.SYNC);

      GraphLayoutEventHandler.handleReactivateViewSynchronization(
          (BinDiffGraph) graphs.getCombinedGraph());
    }
  }

  public void togglePrimaryPerspective() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      viewTabPanel.getNormalViewPanel().togglePrimaryPerspective();
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void toggleProximityBrowsing() {
    BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph =
        (BinDiffGraph) graphs.getCombinedGraph();

    if (settings.isAsync() && settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      graph =
          settings.getFocus() == ESide.PRIMARY
              ? (BinDiffGraph) graphs.getPrimaryGraph()
              : (BinDiffGraph) graphs.getSecondaryGraph();
    }

    if (!settings.getProximitySettings().getProximityBrowsing()) {
      GraphLayoutEventHandler.handleProximityBrowsingActivatedEvent(graph);
    } else {
      GraphLayoutEventHandler.handleProximityBrowsingDeactivatedEvent(graph);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void toggleProximityBrowsingFrozen() {
    final GraphProximityBrowsingSettings proximitySettings = settings.getProximitySettings();

    proximitySettings.setProximityBrowsingFrozen(!proximitySettings.getProximityBrowsingFrozen());

    if (proximitySettings.getProximityBrowsing()
        && !proximitySettings.getProximityBrowsingFrozen()) {
      BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph =
          (BinDiffGraph) graphs.getCombinedGraph();

      if (settings.isAsync() && settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
        graph =
            settings.getFocus() == ESide.PRIMARY
                ? (BinDiffGraph) graphs.getPrimaryGraph()
                : (BinDiffGraph) graphs.getSecondaryGraph();
      }

      GraphLayoutEventHandler.handleSelectionChangedEvent(graph, true);
    }
  }

  public void toggleSecondaryPerspective() {
    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      viewTabPanel.getNormalViewPanel().toggleSecondaryPerspective();
    }
  }

  public void undoSelection() {
    graphs.getPrimaryGraph().getSelectionHistory().setEnabled(false);
    graphs.getSecondaryGraph().getSelectionHistory().setEnabled(false);
    graphs.getCombinedGraph().getSelectionHistory().setEnabled(false);

    // this is always synced (no async behavior)
    graphs.getPrimaryGraph().getSelectionHistory().undo();
    graphs.getSecondaryGraph().getSelectionHistory().undo();
    graphs.getCombinedGraph().getSelectionHistory().undo();

    graphs.getPrimaryGraph().getSelectionHistory().setEnabled(true);
    graphs.getSecondaryGraph().getSelectionHistory().setEnabled(true);
    graphs.getCombinedGraph().getSelectionHistory().setEnabled(true);
  }

  public void writeComments() {
    if (!hasChangedComments) {
      return;
    }
    try {
      final Workspace workspace = getWorkspace();
      final DiffMetaData metadata = graphs.getDiff().getMetaData();
      final ViewData viewData = viewTabPanel.getView();

      final String priMd5 = metadata.getImageHash(ESide.PRIMARY);
      final String secMd5 = metadata.getImageHash(ESide.SECONDARY);

      CommentsWriter.writeComments(workspace, priMd5, secMd5, viewData);
    } catch (final SQLException e) {
      Logger.logException(e, "Couldn't save view comments.");
      CMessageBox.showError(getMainWindow(), "Couldn't save view comments: " + e.getMessage());
    }
  }

  public void writeFlowgraphMatches() {
    if (!hasChangedMatches) {
      return;
    }

    final Diff diff = graphs.getDiff();
    final FlowGraphViewData viewData = (FlowGraphViewData) viewTabPanel.getView();
    final FunctionMatchData functionMatch =
        diff.getMatches().getFunctionMatch(viewData.getAddress(ESide.PRIMARY), ESide.PRIMARY);
    try (final MatchesDatabase matchesDatabase = new MatchesDatabase(diff.getMatchesDatabase())) {
      matchesDatabase.updateFunctionMatch(
          functionMatch.getAddress(ESide.PRIMARY),
          functionMatch.getAddress(ESide.SECONDARY),
          functionMatch);
    } catch (final SQLException e) {
      Logger.logException(e, "Couldn't save changed basic block matches.");
      CMessageBox.showError(
          getMainWindow(), "Couldn't save changed basic block matches." + e.getMessage());
    }
  }

  public void zoom(final boolean zoomIn) {
    BinDiffGraph<?, ?> graph = graphs.getCombinedGraph();

    if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      graph = graphs.getPrimaryGraph();
      if (!settings.isSync() && settings.getFocus() == ESide.SECONDARY) {
        graph = graphs.getSecondaryGraph();
      }
    }

    if (zoomIn) {
      graph.zoomIn();
    } else {
      graph.zoomOut();
    }
  }

  public void zoomToSelectedNodes() {
    if (settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
      GraphZoomer.zoomToNodes(
          graphs.getCombinedGraph(), graphs.getCombinedGraph().getSelectedNodes());
    } else if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
      GraphZoomer.zoomToNodes(graphs.getSuperGraph(), graphs.getSuperGraph().getSelectedNodes());
    }
  }

  private class InternalEditableContentListener implements ILabelEditableContentListener {
    @Override
    public void editableContentChanged(final ZyLabelContent labelContent) {
      setCommentsChanged(true);
    }
  }
}
