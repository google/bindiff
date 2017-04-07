package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.BasicBlockMatchesPie3dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.InstructionMatchesPie3dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.JumpMatchesPie3dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.SimilarityBarChart2dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.subpanels.AddedAndRemovedCalledFunctionsPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.subpanels.IViewsFilterCheckboxListener;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.subpanels.MatchedFunctionViewsFilterPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.IMatchedFunctionsViewsTableListener;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.MatchedFunctionViewsTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.MatchedFunctionsViewsTableModel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffChangeAdapter;
import com.google.security.zynamics.bindiff.project.helpers.GraphGetter;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class MatchedFunctionsTreeNodeContextPanel extends AbstractTreeNodeContextPanel {
  private final Diff diff;

  private final JPanel tablePanel = new JPanel(new BorderLayout());
  private JPanel overviewPanel = new JPanel(new BorderLayout());

  private final MatchedFunctionViewsFilterPanel filterPanel;

  private final AddedAndRemovedCalledFunctionsPanel callerAndCallesPanel;

  private final JumpMatchesPie3dPanel jumpsPiePanel;
  private final BasicBlockMatchesPie3dPanel basicblocksPiePanel;
  private final InstructionMatchesPie3dPanel instructionsPiePanel;
  private final SimilarityBarChart2dPanel similarityBarChartPanel;

  private final MatchedFunctionsViewsTableModel matchedFunctionsTableModel;
  private final MatchedFunctionViewsTable matchedFunctionsTable;

  private final InternalViewsTableListener tableListener = new InternalViewsTableListener();
  private final InternalFilterCheckboxListener filterCheckboxListener =
      new InternalFilterCheckboxListener();
  private final InternalFlowgraphCachedCountsListener countsChangeListener =
      new InternalFlowgraphCachedCountsListener();

  public MatchedFunctionsTreeNodeContextPanel(
      final WorkspaceTabPanelFunctions controller, final Diff diff) {
    Preconditions.checkNotNull(diff);

    this.diff = diff;

    basicblocksPiePanel = new BasicBlockMatchesPie3dPanel(diff, false);
    jumpsPiePanel = new JumpMatchesPie3dPanel(diff, false);
    instructionsPiePanel = new InstructionMatchesPie3dPanel(diff, false);
    similarityBarChartPanel = new SimilarityBarChart2dPanel(diff.getMetaData());

    matchedFunctionsTableModel = new MatchedFunctionsViewsTableModel(diff, true);
    matchedFunctionsTable = new MatchedFunctionViewsTable(matchedFunctionsTableModel, controller);

    filterPanel = new MatchedFunctionViewsFilterPanel(matchedFunctionsTable);
    callerAndCallesPanel =
        new AddedAndRemovedCalledFunctionsPanel(diff, controller, matchedFunctionsTable);

    filterPanel.addListener(filterCheckboxListener);
    matchedFunctionsTable.addListener(tableListener);
    matchedFunctionsTableModel.addListener(tableListener);

    diff.getMetaData().addListener(countsChangeListener);

    initComponents();

    updateCharts(matchedFunctionsTable);
  }

  private JPanel createOverviewPanel() {
    // Outer panel with title and frame
    final JPanel overviewBorderPanel = new JPanel(new BorderLayout(0, 0));
    overviewBorderPanel.setBorder(
        new CompoundBorder(new TitledBorder("Overview"), new LineBorder(Color.GRAY)));

    // Panel that contains the actual chart panels
    final JPanel chartsPanel = new JPanel(new GridLayout(1, 4, 0, 0));
    overviewBorderPanel.add(chartsPanel, BorderLayout.CENTER);

    chartsPanel.add(basicblocksPiePanel);
    chartsPanel.add(jumpsPiePanel);
    chartsPanel.add(instructionsPiePanel);
    chartsPanel.add(similarityBarChartPanel);

    return overviewBorderPanel;
  }

  private JPanel createTablePanel() {
    final JScrollPane scrollpane = new JScrollPane(matchedFunctionsTable);
    final JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.add(scrollpane, BorderLayout.CENTER);

    return tablePanel;
  }

  private void initComponents() {
    tablePanel.setBorder(new TitledBorder(""));
    overviewPanel = createOverviewPanel();

    updateBorderTitle();

    final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
    splitPane.setBorder(null);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(200);

    final JSplitPane innerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
    innerSplitPane.setBorder(null);
    innerSplitPane.setOneTouchExpandable(true);
    innerSplitPane.setResizeWeight(1.);
    innerSplitPane.setDividerLocation(.7);

    tablePanel.add(filterPanel, BorderLayout.NORTH);
    tablePanel.add(createTablePanel(), BorderLayout.CENTER);

    splitPane.setTopComponent(overviewPanel);
    splitPane.setBottomComponent(innerSplitPane);
    innerSplitPane.setTopComponent(tablePanel);
    innerSplitPane.setBottomComponent(callerAndCallesPanel);

    add(splitPane, BorderLayout.CENTER);
  }

  private void updateBorderTitle() {
    ((TitledBorder) tablePanel.getBorder())
        .setTitle(
            String.format(
                "%d / %d Matched Functions",
                matchedFunctionsTable.getRowCount(),
                diff.getMatches().getSizeOfMatchedFunctions()));

    updateUI();
  }

  private void updateCharts(final MatchedFunctionViewsTable table) {
    final Vector<Double> similarities = new Vector<>();
    final Vector<Double> confidences = new Vector<>();

    int[] rows = table.getSelectedRows();

    if (rows.length == 0) {
      rows = new int[table.getRowCount()];

      for (int i = 0; i < table.getRowCount(); i++) {
        rows[i] = i;
      }
    }

    int matchedBasicblocks = 0;
    int primaryUnmatchedBasicblocks = 0;
    int secondaryUnmatchedBasicblocks = 0;

    int matchedJumps = 0;
    int primaryUnmatchedJumps = 0;
    int secondaryUnmatchedJumps = 0;

    int matchedInstructions = 0;
    int primaryUnmatchedInstructions = 0;
    int secondaryUnmatchedInstructions = 0;

    for (final int i : rows) {
      final String primaryValue =
          (String)
              table.getValueAt(
                  i,
                  table.convertColumnIndexToView(MatchedFunctionsViewsTableModel.PRIMARY_ADDRESS));
      final IAddress primaryAddr = new CAddress(primaryValue, 16);
      final String secondaryValue =
          (String)
              table.getValueAt(
                  i,
                  table.convertColumnIndexToView(
                      MatchedFunctionsViewsTableModel.SECONDARY_ADDRESS));
      final IAddress secondaryAddr = new CAddress(secondaryValue, 16);

      final RawFunction priFunction = diff.getFunction(primaryAddr, ESide.PRIMARY);
      final RawFunction secFunction = diff.getFunction(secondaryAddr, ESide.SECONDARY);

      matchedBasicblocks += priFunction.getSizeOfMatchedBasicblocks();
      primaryUnmatchedBasicblocks += priFunction.getSizeOfUnmatchedBasicblocks();
      secondaryUnmatchedBasicblocks += secFunction.getSizeOfUnmatchedBasicblocks();

      matchedJumps += priFunction.getSizeOfMatchedJumps();
      primaryUnmatchedJumps += priFunction.getSizeOfUnmatchedJumps();
      secondaryUnmatchedJumps += secFunction.getSizeOfUnmatchedJumps();

      matchedInstructions += priFunction.getSizeOfMatchedInstructions();
      primaryUnmatchedInstructions += priFunction.getSizeOfUnmatchedInstructions();
      secondaryUnmatchedInstructions += secFunction.getSizeOfUnmatchedInstructions();

      final FunctionMatchData functionMatch = priFunction.getFunctionMatch();

      similarities.add(functionMatch.getSimilarity());
      confidences.add(functionMatch.getConfidence());
    }

    basicblocksPiePanel.updateDataset(
        matchedBasicblocks, primaryUnmatchedBasicblocks, secondaryUnmatchedBasicblocks);
    jumpsPiePanel.updateDataset(matchedJumps, primaryUnmatchedJumps, secondaryUnmatchedJumps);
    instructionsPiePanel.updateDataset(
        matchedInstructions, primaryUnmatchedInstructions, secondaryUnmatchedInstructions);
    similarityBarChartPanel.updateDataset(similarities);
  }

  public void dispose() {
    matchedFunctionsTable.removeListener(tableListener);
    filterPanel.removeListener(filterCheckboxListener);

    matchedFunctionsTable.dispose();

    diff.getMetaData().removeListener(countsChangeListener);
  }

  @Override
  public List<AbstractTable> getTables() {
    final List<AbstractTable> list = new ArrayList<>();
    list.add(matchedFunctionsTable);
    return list;
  }

  private class InternalFilterCheckboxListener implements IViewsFilterCheckboxListener {
    @Override
    public void functionViewsFilterChanged(
        final boolean structuralChange,
        final boolean instructionOnlyChange,
        final boolean identical) {
      final RawCallGraph priCallgraph = diff.getCallgraph(ESide.PRIMARY);
      final RawCallGraph secCallgraph = diff.getCallgraph(ESide.SECONDARY);

      final Set<Pair<RawFunction, RawFunction>> filteredFunctions = new HashSet<>();

      if (!structuralChange && !instructionOnlyChange && !identical) {
        filteredFunctions.addAll(GraphGetter.getMatchedFunctionPairs(priCallgraph, secCallgraph));
      } else {
        if (structuralChange) {
          filteredFunctions.addAll(
              GraphGetter.getStructuralChangedFunctionPairs(priCallgraph, secCallgraph));
        }

        if (instructionOnlyChange) {
          filteredFunctions.addAll(
              GraphGetter.getInstructionOnlyChangedFunctionPairs(priCallgraph, secCallgraph));
        }

        if (identical) {
          filteredFunctions.addAll(
              GraphGetter.getIdenticalFunctionPairs(priCallgraph, secCallgraph));
        }
      }

      ((MatchedFunctionsViewsTableModel) matchedFunctionsTable.getTableModel())
          .setMatchedFunctionPairs(filteredFunctions);
    }
  }

  private class InternalFlowgraphCachedCountsListener extends DiffChangeAdapter {
    @Override
    public void basicblocksCountChanged() {
      updateCharts(matchedFunctionsTable);
    }

    @Override
    public void callsCountChanged() {
      updateCharts(matchedFunctionsTable);
    }

    @Override
    public void instructionsCountsChanged() {
      updateCharts(matchedFunctionsTable);
    }

    @Override
    public void jumpsCountChanged() {
      updateCharts(matchedFunctionsTable);
    }
  }

  private class InternalViewsTableListener implements IMatchedFunctionsViewsTableListener {
    @Override
    public void rowSelectionChanged(final MatchedFunctionViewsTable table) {
      updateCharts(matchedFunctionsTable);
    }

    @Override
    public void tableDataChanged(final MatchedFunctionsViewsTableModel model) {
      updateCharts(matchedFunctionsTable);
      updateBorderTitle();
    }
  }
}
