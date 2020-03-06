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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import static com.google.common.base.Preconditions.checkNotNull;

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
import com.google.security.zynamics.bindiff.project.diff.CountsChangedListener;
import com.google.security.zynamics.bindiff.project.diff.Diff;
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

  private final AddedAndRemovedCalledFunctionsPanel callerAndCalleesPanel;

  private final JumpMatchesPie3dPanel jumpsPiePanel;
  private final BasicBlockMatchesPie3dPanel basicBlocksPiePanel;
  private final InstructionMatchesPie3dPanel instructionsPiePanel;
  private final SimilarityBarChart2dPanel similarityBarChartPanel;

  private final MatchedFunctionsViewsTableModel matchedFunctionsTableModel;
  private final MatchedFunctionViewsTable matchedFunctionsTable;

  private final InternalViewsTableListener tableListener = new InternalViewsTableListener();
  private final InternalFilterCheckboxListener filterCheckboxListener =
      new InternalFilterCheckboxListener();
  private final InternalFlowGraphCachedCountsListener countsChangeListener =
      new InternalFlowGraphCachedCountsListener();

  public MatchedFunctionsTreeNodeContextPanel(
      final WorkspaceTabPanelFunctions controller, final Diff diff) {
    checkNotNull(diff);

    this.diff = diff;

    basicBlocksPiePanel = new BasicBlockMatchesPie3dPanel(diff, false);
    jumpsPiePanel = new JumpMatchesPie3dPanel(diff, false);
    instructionsPiePanel = new InstructionMatchesPie3dPanel(diff, false);
    similarityBarChartPanel = new SimilarityBarChart2dPanel(diff.getMetadata());

    matchedFunctionsTableModel = new MatchedFunctionsViewsTableModel(diff, true);
    matchedFunctionsTable = new MatchedFunctionViewsTable(matchedFunctionsTableModel, controller);

    filterPanel = new MatchedFunctionViewsFilterPanel(matchedFunctionsTable);
    callerAndCalleesPanel =
        new AddedAndRemovedCalledFunctionsPanel(diff, controller, matchedFunctionsTable);

    filterPanel.addListener(filterCheckboxListener);
    matchedFunctionsTable.addListener(tableListener);
    matchedFunctionsTableModel.addListener(tableListener);

    diff.getMetadata().addListener(countsChangeListener);

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

    chartsPanel.add(basicBlocksPiePanel);
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
    innerSplitPane.setBottomComponent(callerAndCalleesPanel);

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

    int[] rows = table.getSelectedRows();

    if (rows.length == 0) {
      rows = new int[table.getRowCount()];

      for (int i = 0; i < table.getRowCount(); i++) {
        rows[i] = i;
      }
    }

    int matchedBasicBlocks = 0;
    int primaryUnmatchedBasicBlocks = 0;
    int secondaryUnmatchedBasicBlocks = 0;

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

      matchedBasicBlocks += priFunction.getSizeOfMatchedBasicBlocks();
      primaryUnmatchedBasicBlocks += priFunction.getSizeOfUnmatchedBasicBlocks();
      secondaryUnmatchedBasicBlocks += secFunction.getSizeOfUnmatchedBasicBlocks();

      matchedJumps += priFunction.getSizeOfMatchedJumps();
      primaryUnmatchedJumps += priFunction.getSizeOfUnmatchedJumps();
      secondaryUnmatchedJumps += secFunction.getSizeOfUnmatchedJumps();

      matchedInstructions += priFunction.getSizeOfMatchedInstructions();
      primaryUnmatchedInstructions += priFunction.getSizeOfUnmatchedInstructions();
      secondaryUnmatchedInstructions += secFunction.getSizeOfUnmatchedInstructions();

      final FunctionMatchData functionMatch = priFunction.getFunctionMatch();

      similarities.add(functionMatch.getSimilarity());
    }

    basicBlocksPiePanel.updateDataset(
        matchedBasicBlocks, primaryUnmatchedBasicBlocks, secondaryUnmatchedBasicBlocks);
    jumpsPiePanel.updateDataset(matchedJumps, primaryUnmatchedJumps, secondaryUnmatchedJumps);
    instructionsPiePanel.updateDataset(
        matchedInstructions, primaryUnmatchedInstructions, secondaryUnmatchedInstructions);
    similarityBarChartPanel.updateDataset(similarities);
  }

  public void dispose() {
    matchedFunctionsTable.removeListener(tableListener);
    filterPanel.removeListener(filterCheckboxListener);

    matchedFunctionsTable.dispose();

    diff.getMetadata().removeListener(countsChangeListener);
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
      final RawCallGraph priCallGraph = diff.getCallGraph(ESide.PRIMARY);
      final RawCallGraph secCallGraph = diff.getCallGraph(ESide.SECONDARY);

      final Set<Pair<RawFunction, RawFunction>> filteredFunctions = new HashSet<>();

      if (!structuralChange && !instructionOnlyChange && !identical) {
        filteredFunctions.addAll(GraphGetter.getMatchedFunctionPairs(priCallGraph, secCallGraph));
      } else {
        if (structuralChange) {
          filteredFunctions.addAll(
              GraphGetter.getStructuralChangedFunctionPairs(priCallGraph, secCallGraph));
        }

        if (instructionOnlyChange) {
          filteredFunctions.addAll(
              GraphGetter.getInstructionOnlyChangedFunctionPairs(priCallGraph, secCallGraph));
        }

        if (identical) {
          filteredFunctions.addAll(
              GraphGetter.getIdenticalFunctionPairs(priCallGraph, secCallGraph));
        }
      }

      ((MatchedFunctionsViewsTableModel) matchedFunctionsTable.getTableModel())
          .setMatchedFunctionPairs(filteredFunctions);
    }
  }

  private class InternalFlowGraphCachedCountsListener extends CountsChangedListener {
    @Override
    public void basicBlocksCountChanged() {
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
