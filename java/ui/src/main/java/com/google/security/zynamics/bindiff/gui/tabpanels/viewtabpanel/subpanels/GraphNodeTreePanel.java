package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.subpanels;

import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.ESortByCriterion;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeMultiFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.EMatchStateFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.ESelectionFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.ESideFilter;
import com.google.security.zynamics.bindiff.graph.filter.enums.EVisibilityFilter;
import com.google.security.zynamics.bindiff.gui.components.treesearchfield.ITreeSearchFieldListener;
import com.google.security.zynamics.bindiff.gui.components.treesearchfield.TreeSearchFieldCombo;
import com.google.security.zynamics.bindiff.gui.dialogs.graphnodetreeoptionsdialog.GraphNodeTreeOptionsDialog;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.AbstractGraphNodeTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.CombinedCallGraphTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.CombinedFlowGraphTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.SingleCallGraphTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.SingleFlowGraphTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.searcher.TreeNodeSearcher;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.TreeNodeMultiSorter;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.treenodes.AbstractRootTreeNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.userview.ViewData;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.TreeNode;

public class GraphNodeTreePanel extends JPanel {
  private static final ImageIcon ICON_OPTIONS =
      ImageUtils.getImageIcon("data/buttonicons/options.png");
  private static final ImageIcon ICON_CLEAR = ImageUtils.getImageIcon("data/buttonicons/clear.png");
  private static final ImageIcon ICON_CLEAR_GRAY =
      ImageUtils.getImageIcon("data/buttonicons/clear-gray.png");

  private final TreeSearchFieldCombo filterCombo = new TreeSearchFieldCombo();

  private final JButton clearButton = new JButton(ICON_CLEAR_GRAY);
  private final JButton optionsButton = new JButton(ICON_OPTIONS);

  private final InternalClearButtonListener clearButtonListener = new InternalClearButtonListener();
  private final InternalOptionsButtonListener optionsButtonAction =
      new InternalOptionsButtonListener();
  private final InternalSearchFieldListener searchComboListener = new InternalSearchFieldListener();

  private final GraphNodeTreeOptionsDialog optionsDialog;

  private AbstractGraphNodeTree tree;

  public GraphNodeTreePanel(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final CombinedGraph combinedGraph) {
    super(new BorderLayout());

    optionsDialog = createOptionsDialog(controller.getMainWindow(), combinedGraph);
    tree = createCombinedTree(controller, diff, view, combinedGraph);

    init();
  }

  public GraphNodeTreePanel(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final SingleGraph singleGraph) {
    super(new BorderLayout());

    optionsDialog = createOptionsDialog(controller.getMainWindow(), singleGraph);
    tree = createSingleTree(controller, diff, view, singleGraph);

    init();
  }

  private void addListeners() {
    filterCombo.addListener(searchComboListener);
    optionsButton.addActionListener(optionsButtonAction);
    clearButton.addActionListener(clearButtonListener);
  }

  private AbstractGraphNodeTree createCombinedTree(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final CombinedGraph combinedGraph) {
    final TreeNodeSearcher searcher = createDefaultTreeSearcher();
    final GraphNodeMultiFilter filter = createDefaultMultiFilter(diff, view);
    final TreeNodeMultiSorter sorter = createDefaultMultiSorter();

    if (combinedGraph.getGraphType() == EGraphType.CALLGRAPH) {
      return new CombinedCallGraphTree(
          controller, diff, view, combinedGraph, searcher, filter, sorter);
    } else if (combinedGraph.getGraphType() == EGraphType.FLOWGRAPH) {
      return new CombinedFlowGraphTree(
          controller, diff, view, combinedGraph, searcher, filter, sorter);
    }

    throw new IllegalStateException("Combined graph node tree cannot be null.");
  }

  @SuppressWarnings("unchecked")
  private GraphNodeMultiFilter createDefaultMultiFilter(final Diff diff, final ViewData view) {
    RawFlowGraph priFlowgraph = null;
    RawFlowGraph secFlowgraph = null;

    if (view.isFlowgraphView()) {
      priFlowgraph = (RawFlowGraph) view.getRawGraph(ESide.PRIMARY);
      secFlowgraph = (RawFlowGraph) view.getRawGraph(ESide.SECONDARY);
    }

    return new GraphNodeMultiFilter(
        diff,
        priFlowgraph,
        secFlowgraph,
        new CAddress(0),
        new CAddress(0xFFFFFFFF),
        EMatchStateFilter.NONE,
        ESelectionFilter.NONE,
        EVisibilityFilter.NONE,
        ESideFilter.NONE);
  }

  private TreeNodeMultiSorter createDefaultMultiSorter() {
    final TreeNodeMultiSorter sorter = new TreeNodeMultiSorter();

    for (int i = 0; i < TreeNodeMultiSorter.MAX_DEPTH; ++i) {
      final ESortByCriterion sortBy = optionsDialog.getSortByCriterion(i);
      final ESortOrder sortOrder = optionsDialog.getSortOrder(i);

      sorter.setCriterion(sortBy, sortOrder, i, false);
    }

    return sorter;
  }

  private TreeNodeSearcher createDefaultTreeSearcher() {
    return new TreeNodeSearcher(false, false, true, true, false, false);
  }

  private GraphNodeTreeOptionsDialog createOptionsDialog(
      final Window parent, final CombinedGraph combinedGraph) {
    final boolean isCallgraph = combinedGraph.getGraphType() == EGraphType.CALLGRAPH;

    final GraphNodeTreeOptionsDialog dlg =
        new GraphNodeTreeOptionsDialog(parent, "Combined Tree Options", isCallgraph, true);
    GuiHelper.centerChildToParent(parent, dlg, true);

    return dlg;
  }

  private GraphNodeTreeOptionsDialog createOptionsDialog(
      final Window parent, final SingleGraph singleGraph) {
    final boolean isCallgraph = singleGraph.getGraphType() == EGraphType.CALLGRAPH;

    final String priTitel = "Primary Tree Options";
    final String secTitel = "Secondary Tree Options";

    final ESide side = singleGraph.getSide();

    final GraphNodeTreeOptionsDialog dlg =
        new GraphNodeTreeOptionsDialog(
            parent, side == ESide.PRIMARY ? priTitel : secTitel, isCallgraph, false);
    GuiHelper.centerChildToParent(parent, dlg, true);

    return dlg;
  }

  private AbstractGraphNodeTree createSingleTree(
      final ViewTabPanelFunctions controller,
      final Diff diff,
      final ViewData view,
      final SingleGraph singleGraph) {
    final TreeNodeSearcher searcher = createDefaultTreeSearcher();
    final GraphNodeMultiFilter filter = createDefaultMultiFilter(diff, view);
    final TreeNodeMultiSorter sorter = createDefaultMultiSorter();

    if (singleGraph.getGraphType() == EGraphType.CALLGRAPH) {
      return new SingleCallGraphTree(controller, diff, view, singleGraph, searcher, filter, sorter);
    } else if (singleGraph.getGraphType() == EGraphType.FLOWGRAPH) {
      return new SingleFlowGraphTree(controller, diff, view, singleGraph, searcher, filter, sorter);
    }

    throw new IllegalStateException("Single graph node tree cannot be null.");
  }

  private void init() {
    clearButton.setToolTipText("Clear Search Results");
    optionsButton.setToolTipText("Search Settings");

    addListeners();

    setBorder(new LineBorder(Color.GRAY));

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(1, 1, 0, 0));

    final JPanel comboPanel = new JPanel(new BorderLayout());
    comboPanel.setBackground(Color.WHITE);
    comboPanel.setBorder(new EmptyBorder(0, 0, 1, 1));

    comboPanel.add(filterCombo, BorderLayout.CENTER);

    clearButton.setBackground(Color.WHITE);
    clearButton.setFocusable(false);
    clearButton.setPreferredSize(new Dimension(32, filterCombo.getPreferredSize().height));

    final JPanel clearButtonPanel = new JPanel(new BorderLayout());
    clearButtonPanel.setBorder(new EmptyBorder(0, 1, 0, 0));
    clearButtonPanel.add(clearButton, BorderLayout.CENTER);

    comboPanel.add(clearButtonPanel, BorderLayout.EAST);

    optionsButton.setBackground(Color.WHITE);
    optionsButton.setFocusable(false);
    optionsButton.setPreferredSize(new Dimension(32, filterCombo.getPreferredSize().height));

    final JPanel optionsButtonPanel = new JPanel(new BorderLayout());
    optionsButtonPanel.setBorder(new EmptyBorder(0, 1, 0, 0));
    optionsButtonPanel.add(optionsButton, BorderLayout.EAST);

    clearButtonPanel.add(optionsButtonPanel, BorderLayout.EAST);

    filterCombo.setBackground(Color.WHITE);
    filterCombo.setBorder(new EmptyBorder(0, 0, 0, 0));

    tree.setBorder(new EmptyBorder(1, 1, 1, 1));

    panel.add(comboPanel, BorderLayout.NORTH);

    final JScrollPane scrollPane = new JScrollPane(tree);
    panel.add(scrollPane, BorderLayout.CENTER);

    add(panel, BorderLayout.CENTER);
  }

  public void dispose() {
    if (tree instanceof SingleCallGraphTree) {
      ((SingleCallGraphTree) tree).dispose();
    } else if (tree instanceof SingleFlowGraphTree) {
      ((SingleFlowGraphTree) tree).dispose();
    } else if (tree instanceof CombinedCallGraphTree) {
      ((CombinedCallGraphTree) tree).dispose();
    } else if (tree instanceof CombinedFlowGraphTree) {
      ((CombinedFlowGraphTree) tree).dispose();
    }

    optionsDialog.dispose();

    filterCombo.removeListener(searchComboListener);
    optionsButton.removeActionListener(optionsButtonAction);
    clearButton.removeActionListener(clearButtonListener);

    tree = null;
  }

  public AbstractGraphNodeTree getTree() {
    return tree;
  }

  public void updateClearTreeSearchIcons() {
    clearButton.setIcon(ICON_CLEAR);

    final TreeNode baseNode = getTree().getRootNode().getFirstChild();
    if (baseNode != null) {
      if (baseNode.getChildCount() == getTree().getGraph().getNodes().size()) {
        clearButton.setIcon(ICON_CLEAR_GRAY);
      }
    }
  }

  private class InternalClearButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final GraphNodeMultiFilter filter =
          ((AbstractRootTreeNode) tree.getModel().getRoot()).getFilter();
      filter.clearSettings(false);

      optionsDialog.setDefaults(1);

      final TreeNodeSearcher searcher =
          ((AbstractRootTreeNode) tree.getModel().getRoot()).getSearcher();
      searcher.setSearchString("");

      filterCombo.clear();

      clearButton.setIcon(ICON_CLEAR_GRAY);
    }
  }

  private class InternalOptionsButtonListener implements ActionListener {
    private void setFilter(final boolean notify) {
      final GraphNodeMultiFilter filter =
          ((AbstractRootTreeNode) tree.getModel().getRoot()).getFilter();

      filter.setFilter(
          optionsDialog.getStartAddress(),
          optionsDialog.getEndAddress(),
          optionsDialog.getMatchStateFilter(),
          optionsDialog.getSelectionFilter(),
          optionsDialog.getVisibilityFilter(),
          optionsDialog.getSideFilter(),
          false);

      if (notify) {
        filter.notifyListeners();
      }
    }

    private void setSearcher(final boolean notify) {
      final TreeNodeSearcher searcher =
          ((AbstractRootTreeNode) tree.getModel().getRoot()).getSearcher();

      final boolean regEx = optionsDialog.getRegEx();
      final boolean caseSensitive = optionsDialog.getCaseSensitive();

      final boolean priSide = optionsDialog.getPrimarySide();
      final boolean secSide = optionsDialog.getSecondarySide();

      final boolean highlighting = optionsDialog.getHighlightGraphNodes();
      final boolean useTemporaryResults = optionsDialog.getUseTemporaryResult();

      searcher.setSearchSettings(
          regEx, caseSensitive, priSide, secSide, useTemporaryResults, highlighting);

      if (notify) {
        searcher.notifyListeners();
      }
    }

    private void setSorter(final boolean notify) {
      final TreeNodeMultiSorter sorter =
          ((AbstractRootTreeNode) tree.getModel().getRoot()).getSorter();

      for (int depth = 0; depth < TreeNodeMultiSorter.MAX_DEPTH; ++depth) {
        final ESortByCriterion sortBy = optionsDialog.getSortByCriterion(depth);
        final ESortOrder sortOrder = optionsDialog.getSortOrder(depth);

        sorter.setCriterion(sortBy, sortOrder, depth, false);
      }

      if (notify) {
        sorter.notifyListeners();
      }
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getSource().equals(optionsButton)) {
        optionsDialog.setVisible(true);

        if (optionsDialog.getOkPressed()) {
          setSearcher(false);
          setFilter(false);
          setSorter(true);

          updateClearTreeSearchIcons();
        }
      }
    }
  }

  private class InternalSearchFieldListener implements ITreeSearchFieldListener {
    @Override
    public void searchChanged(final String searchText) {
      final TreeNodeSearcher searcher =
          ((AbstractRootTreeNode) tree.getModel().getRoot()).getSearcher();
      searcher.setSearchString(searchText);

      updateClearTreeSearchIcons();
    }
  }
}
