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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.BasicBlockMatchesPie3dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.CallMatchesPie3dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.FunctionMatchesPie3dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.InstructionMatchesPie3dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.JumpMatchesPie3dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.charts.SimilarityBarChart2dPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarExtendedCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarExtendedLabel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarLabel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.CallGraphViewTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.CallGraphViewTableModel;
import com.google.security.zynamics.bindiff.project.diff.CountsChangedListener;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class CallGraphsTreeNodeContextPanel extends AbstractTreeNodeContextPanel {
  private static final int TEXTFIELD_HEIGHT = 25;
  private static final int TEXTFIELD_LABEL_WIDTH = 100;

  private final Diff diff;

  private final CallGraphViewTable callGraphTable;

  private final InternalFlowgraphCachedCountsListener countsChangeListener =
      new InternalFlowgraphCachedCountsListener();

  private PercentageTwoBarExtendedLabel primaryFunctions;
  private PercentageTwoBarExtendedLabel secondaryFunctions;

  private PercentageTwoBarExtendedLabel primaryCalls;
  private PercentageTwoBarExtendedLabel secondaryCalls;

  private PercentageTwoBarLabel primaryBasicBlocks;
  private PercentageTwoBarLabel secondaryBasicBlocks;

  private PercentageTwoBarLabel primaryJumps;
  private PercentageTwoBarLabel secondaryJumps;

  private PercentageTwoBarLabel primaryInstructions;
  private PercentageTwoBarLabel secondaryInstructions;

  public CallGraphsTreeNodeContextPanel(
      final Diff diff, final WorkspaceTabPanelFunctions controller) {
    checkNotNull(diff);

    this.diff = diff;
    callGraphTable = new CallGraphViewTable(new CallGraphViewTableModel(diff), controller);

    init();
    diff.getMetadata().addListener(countsChangeListener);
  }

  private JPanel createChartsPanel() {
    // Outer panel with title and frame
    final JPanel overviewPanel = new JPanel(new BorderLayout(0, 0));
    overviewPanel.setBorder(
        new CompoundBorder(new TitledBorder("Overview"), new LineBorder(Color.GRAY)));

    // Panel that contains the actual chart panels
    final JPanel chartsPanel = new JPanel(new GridLayout(2, 3, 0, 0));
    chartsPanel.setBackground(Color.WHITE);
    overviewPanel.add(chartsPanel, BorderLayout.CENTER);

    // Chart panels with fixed sizes
    chartsPanel.add(new FunctionMatchesPie3dPanel(diff));
    chartsPanel.add(new CallMatchesPie3dPanel(diff));
    chartsPanel.add(new SimilarityBarChart2dPanel(diff.getDiffMetaData()));
    chartsPanel.add(new BasicBlockMatchesPie3dPanel(diff, true));
    chartsPanel.add(new JumpMatchesPie3dPanel(diff, true));
    chartsPanel.add(new InstructionMatchesPie3dPanel(diff, true));

    return overviewPanel;
  }

  private JPanel createInfoPanel() {
    final MatchData matches = diff.getMatches();

    final int matchedFunctions = matches.getSizeOfMatchedFunctions();
    final int changedFunctions = matches.getSizeOfChangedFunctions();
    final int primaryUnmatchedFunctions = matches.getSizeOfUnmatchedFunctions(ESide.PRIMARY);
    final int secondaryUnmatchedFunctions = matches.getSizeOfUnmatchedFunctions(ESide.SECONDARY);
    final PercentageTwoBarExtendedCellData primaryMatchedFunctionsData =
        new PercentageTwoBarExtendedCellData(
            matchedFunctions, changedFunctions, primaryUnmatchedFunctions);
    final PercentageTwoBarExtendedCellData secondaryMatchedFunctionsData =
        new PercentageTwoBarExtendedCellData(
            matchedFunctions, changedFunctions, secondaryUnmatchedFunctions);
    primaryFunctions =
        new PercentageTwoBarExtendedLabel(
            primaryMatchedFunctionsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.TABLE_CELL_CHANGED_BACKGROUND,
            Colors.UNMATCHED_PRIMARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);
    secondaryFunctions =
        new PercentageTwoBarExtendedLabel(
            secondaryMatchedFunctionsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.TABLE_CELL_CHANGED_BACKGROUND,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);

    final int matchedCalls = matches.getSizeOfMatchedCalls();
    final int changedCalls = matches.getSizeOfChangedCalls();
    final int primaryUnmatchedCalls = matches.getSizeOfUnmatchedCalls(ESide.PRIMARY);
    final int secondaryUnmatchedCalls = matches.getSizeOfUnmatchedCalls(ESide.SECONDARY);
    final PercentageTwoBarExtendedCellData primaryCallsData =
        new PercentageTwoBarExtendedCellData(matchedCalls, changedCalls, primaryUnmatchedCalls);
    final PercentageTwoBarExtendedCellData secondaryCallsData =
        new PercentageTwoBarExtendedCellData(matchedCalls, changedCalls, secondaryUnmatchedCalls);
    primaryCalls =
        new PercentageTwoBarExtendedLabel(
            primaryCallsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.TABLE_CELL_CHANGED_BACKGROUND,
            Colors.UNMATCHED_PRIMARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);
    secondaryCalls =
        new PercentageTwoBarExtendedLabel(
            secondaryCallsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.TABLE_CELL_CHANGED_BACKGROUND,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);

    final int matchedBasicBlocks = matches.getSizeOfMatchedBasicBlocks();
    final int primaryUnmatchedBasicBlocks = matches.getSizeOfUnmatchedBasicBlocks(ESide.PRIMARY);
    final int secondaryUnmatchedBasicBlocks =
        matches.getSizeOfUnmatchedBasicBlocks(ESide.SECONDARY);
    final PercentageTwoBarCellData primaryMatchedBasicBlocksData =
        new PercentageTwoBarCellData(matchedBasicBlocks, primaryUnmatchedBasicBlocks);
    final PercentageTwoBarCellData secondaryMatchedBasicBlocksData =
        new PercentageTwoBarCellData(matchedBasicBlocks, secondaryUnmatchedBasicBlocks);
    primaryBasicBlocks =
        new PercentageTwoBarLabel(
            primaryMatchedBasicBlocksData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_PRIMARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);
    secondaryBasicBlocks =
        new PercentageTwoBarLabel(
            secondaryMatchedBasicBlocksData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);

    final int matchedJumps = matches.getSizeOfMatchedJumps();
    final int primaryUnmatchedJumps = matches.getSizeOfUnmatchedJumps(ESide.PRIMARY);
    final int secondaryUnmatchedJumps = matches.getSizeOfUnmatchedJumps(ESide.SECONDARY);
    final PercentageTwoBarCellData primaryMatchedJumpsData =
        new PercentageTwoBarCellData(matchedJumps, primaryUnmatchedJumps);
    final PercentageTwoBarCellData secondaryMatchedJumpsData =
        new PercentageTwoBarCellData(matchedJumps, secondaryUnmatchedJumps);
    primaryJumps =
        new PercentageTwoBarLabel(
            primaryMatchedJumpsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_PRIMARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);
    secondaryJumps =
        new PercentageTwoBarLabel(
            secondaryMatchedJumpsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);

    final int matchedInstructions = matches.getSizeOfMatchedInstructions();
    final int primaryUnmatchedInstructins = matches.getSizeOfUnmatchedInstructions(ESide.PRIMARY);
    final int secondaryUnmatchedInstructios =
        matches.getSizeOfUnmatchedInstructions(ESide.SECONDARY);
    final PercentageTwoBarCellData primaryMatchedInstructionsData =
        new PercentageTwoBarCellData(matchedInstructions, primaryUnmatchedInstructins);
    final PercentageTwoBarCellData secondaryMatchedInstructionsData =
        new PercentageTwoBarCellData(matchedInstructions, secondaryUnmatchedInstructios);
    primaryInstructions =
        new PercentageTwoBarLabel(
            primaryMatchedInstructionsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_PRIMARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);
    secondaryInstructions =
        new PercentageTwoBarLabel(
            secondaryMatchedInstructionsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELD_HEIGHT);

    final JLabel primaryName = new JLabel(diff.getMetadata().getImageName(ESide.PRIMARY));
    final JLabel secondaryName = new JLabel(diff.getMetadata().getImageName(ESide.SECONDARY));

    final JPanel infoPanel = new JPanel(new GridLayout(1, 2, 2, 2));

    final JPanel primaryOuterPanel = new JPanel(new BorderLayout());
    primaryOuterPanel.setBorder(new TitledBorder("Primary Call Graph"));

    final JPanel primaryPanel = new JPanel(new GridLayout(6, 1, 2, 2));
    primaryPanel.setBorder(new LineBorder(Color.GRAY));
    primaryPanel.setBackground(Color.WHITE);
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Image Name", TEXTFIELD_LABEL_WIDTH, primaryName, TEXTFIELD_HEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Functions", TEXTFIELD_LABEL_WIDTH, primaryFunctions, TEXTFIELD_HEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Calls", TEXTFIELD_LABEL_WIDTH, primaryCalls, TEXTFIELD_HEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Basic Blocks", TEXTFIELD_LABEL_WIDTH, primaryBasicBlocks, TEXTFIELD_HEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Jumps", TEXTFIELD_LABEL_WIDTH, primaryJumps, TEXTFIELD_HEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Instructions", TEXTFIELD_LABEL_WIDTH, primaryInstructions, TEXTFIELD_HEIGHT));

    primaryOuterPanel.add(primaryPanel, BorderLayout.CENTER);

    final JPanel secondaryOuterPanel = new JPanel(new BorderLayout());
    secondaryOuterPanel.setBorder(new TitledBorder("Secondary Call Graph"));

    final JPanel secondaryPanel = new JPanel(new GridLayout(6, 1, 2, 2));
    secondaryPanel.setBorder(new LineBorder(Color.GRAY));
    secondaryPanel.setBackground(Color.WHITE);
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Image Name", TEXTFIELD_LABEL_WIDTH, secondaryName, TEXTFIELD_HEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Functions", TEXTFIELD_LABEL_WIDTH, secondaryFunctions, TEXTFIELD_HEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Calls", TEXTFIELD_LABEL_WIDTH, secondaryCalls, TEXTFIELD_HEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Basic Blocks", TEXTFIELD_LABEL_WIDTH, secondaryBasicBlocks, TEXTFIELD_HEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Jumps", TEXTFIELD_LABEL_WIDTH, secondaryJumps, TEXTFIELD_HEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Instructions", TEXTFIELD_LABEL_WIDTH, secondaryInstructions, TEXTFIELD_HEIGHT));

    secondaryOuterPanel.add(secondaryPanel, BorderLayout.CENTER);

    infoPanel.add(primaryOuterPanel);
    infoPanel.add(secondaryOuterPanel);

    return infoPanel;
  }

  private JPanel createTablePanel() {
    final JScrollPane callGraphScrollPane = new JScrollPane(callGraphTable);
    callGraphScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    final JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.setBorder(new TitledBorder("Call Graph View"));

    tablePanel.add(callGraphScrollPane, BorderLayout.CENTER);
    tablePanel.setPreferredSize(new Dimension(tablePanel.getPreferredSize().width, 70));

    return tablePanel;
  }

  private void init() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(createChartsPanel(), BorderLayout.NORTH);
    panel.add(createInfoPanel(), BorderLayout.CENTER);
    panel.add(createTablePanel(), BorderLayout.SOUTH);

    add(panel, BorderLayout.NORTH);
  }

  public void dipose() {
    diff.getMetadata().removeListener(countsChangeListener);
  }

  @Override
  public List<AbstractTable> getTables() {
    final List<AbstractTable> list = new ArrayList<>();
    list.add(callGraphTable);
    return list;
  }

  private class InternalFlowgraphCachedCountsListener extends CountsChangedListener {
    @Override
    public void basicBlocksCountChanged() {
      final MatchData matches = diff.getMatches();

      final int matchedBasicblocks = matches.getSizeOfMatchedBasicBlocks();
      final int primaryUnmatchedBasicblocks = matches.getSizeOfUnmatchedBasicBlocks(ESide.PRIMARY);
      final int secondaryUnmatchedBasicblocks =
          matches.getSizeOfUnmatchedBasicBlocks(ESide.SECONDARY);

      primaryBasicBlocks.updateData(matchedBasicblocks, primaryUnmatchedBasicblocks);
      secondaryBasicBlocks.updateData(matchedBasicblocks, secondaryUnmatchedBasicblocks);
    }

    @Override
    public void callsCountChanged() {
      final MatchData matches = diff.getMatches();

      final int matchedCalls = matches.getSizeOfMatchedCalls();
      final int changedCalls = matches.getSizeOfChangedCalls();
      final int primaryUnmatchedCalls = matches.getSizeOfUnmatchedCalls(ESide.PRIMARY);
      final int secondaryUnmatchedCalls = matches.getSizeOfUnmatchedCalls(ESide.SECONDARY);

      primaryCalls.updateData(matchedCalls, changedCalls, primaryUnmatchedCalls);
      secondaryCalls.updateData(matchedCalls, changedCalls, secondaryUnmatchedCalls);
    }

    @Override
    public void functionsCountChanged() {
      final MatchData matches = diff.getMatches();

      final int matchedFunctions = matches.getSizeOfMatchedFunctions();
      final int changedFunctions = matches.getSizeOfChangedFunctions();
      final int primaryUnmatchedFunctions = matches.getSizeOfUnmatchedFunctions(ESide.PRIMARY);
      final int secondaryUnmatchedFunctions = matches.getSizeOfUnmatchedFunctions(ESide.SECONDARY);

      primaryFunctions.updateData(matchedFunctions, changedFunctions, primaryUnmatchedFunctions);
      secondaryFunctions.updateData(
          matchedFunctions, changedFunctions, secondaryUnmatchedFunctions);
    }

    @Override
    public void instructionsCountsChanged() {
      final MatchData matches = diff.getMatches();

      final int matchedInstructions = matches.getSizeOfMatchedInstructions();
      final int primaryUnmatchedInstructins = matches.getSizeOfUnmatchedInstructions(ESide.PRIMARY);
      final int secondaryUnmatchedInstructios =
          matches.getSizeOfUnmatchedInstructions(ESide.SECONDARY);

      primaryInstructions.updateData(matchedInstructions, primaryUnmatchedInstructins);
      secondaryInstructions.updateData(matchedInstructions, secondaryUnmatchedInstructios);
    }

    @Override
    public void jumpsCountChanged() {
      final MatchData matches = diff.getMatches();

      final int matchedJumps = matches.getSizeOfMatchedJumps();
      final int primaryUnmatchedJumps = matches.getSizeOfUnmatchedJumps(ESide.PRIMARY);
      final int secondaryUnmatchedJumps = matches.getSizeOfUnmatchedJumps(ESide.SECONDARY);

      primaryJumps.updateData(matchedJumps, primaryUnmatchedJumps);
      secondaryJumps.updateData(matchedJumps, secondaryUnmatchedJumps);
    }
  }
}
