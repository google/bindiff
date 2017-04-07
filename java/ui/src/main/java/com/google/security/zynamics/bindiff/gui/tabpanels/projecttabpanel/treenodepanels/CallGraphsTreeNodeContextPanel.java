package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import com.google.common.base.Preconditions;
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
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.diff.DiffChangeAdapter;
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
  private static final int TEXTFIELDHEIGHT = 25;
  private static final int TEXTFIELDLABELWIDTH = 100;

  private final Diff diff;

  private final CallGraphViewTable callgraphTable;

  private final InternalFlowgraphCachedCountsListener countsChangeListener =
      new InternalFlowgraphCachedCountsListener();

  private PercentageTwoBarExtendedLabel primaryFunctions;
  private PercentageTwoBarExtendedLabel secondaryFunctions;

  private PercentageTwoBarExtendedLabel primaryCalls;
  private PercentageTwoBarExtendedLabel secondaryCalls;

  private PercentageTwoBarLabel primaryBasicblocks;
  private PercentageTwoBarLabel secondaryBasicblocks;

  private PercentageTwoBarLabel primaryJumps;
  private PercentageTwoBarLabel secondaryJumps;

  private PercentageTwoBarLabel primaryInstructions;
  private PercentageTwoBarLabel secondaryInstructions;

  public CallGraphsTreeNodeContextPanel(
      final Diff diff, final WorkspaceTabPanelFunctions controller) {
    Preconditions.checkNotNull(diff);

    this.diff = diff;
    callgraphTable = new CallGraphViewTable(new CallGraphViewTableModel(diff), controller);

    init();
    diff.getMetaData().addListener(countsChangeListener);
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
            TEXTFIELDHEIGHT);
    secondaryFunctions =
        new PercentageTwoBarExtendedLabel(
            secondaryMatchedFunctionsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.TABLE_CELL_CHANGED_BACKGROUND,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELDHEIGHT);

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
            TEXTFIELDHEIGHT);
    secondaryCalls =
        new PercentageTwoBarExtendedLabel(
            secondaryCallsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.TABLE_CELL_CHANGED_BACKGROUND,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELDHEIGHT);

    final int matchedBasicblocks = matches.getSizeOfMatchedBasicblocks();
    final int primaryUnmatchedBasicblocks = matches.getSizeOfUnmatchedBasicblocks(ESide.PRIMARY);
    final int secondaryUnmatchedBasicblocks =
        matches.getSizeOfUnmatchedBasicblocks(ESide.SECONDARY);
    final PercentageTwoBarCellData primaryMatchedBasicblocksData =
        new PercentageTwoBarCellData(matchedBasicblocks, primaryUnmatchedBasicblocks);
    final PercentageTwoBarCellData secondaryMatchedBasicblocksData =
        new PercentageTwoBarCellData(matchedBasicblocks, secondaryUnmatchedBasicblocks);
    primaryBasicblocks =
        new PercentageTwoBarLabel(
            primaryMatchedBasicblocksData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_PRIMARY_LABEL_BAR,
            TEXTFIELDHEIGHT);
    secondaryBasicblocks =
        new PercentageTwoBarLabel(
            secondaryMatchedBasicblocksData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELDHEIGHT);

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
            TEXTFIELDHEIGHT);
    secondaryJumps =
        new PercentageTwoBarLabel(
            secondaryMatchedJumpsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELDHEIGHT);

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
            TEXTFIELDHEIGHT);
    secondaryInstructions =
        new PercentageTwoBarLabel(
            secondaryMatchedInstructionsData,
            Colors.MATCHED_LABEL_BAR,
            Colors.UNMATCHED_SECONDARY_LABEL_BAR,
            TEXTFIELDHEIGHT);

    final JLabel primaryName = new JLabel(diff.getMetaData().getImageName(ESide.PRIMARY));
    final JLabel secondaryName = new JLabel(diff.getMetaData().getImageName(ESide.SECONDARY));

    final JPanel infoPanel = new JPanel(new GridLayout(1, 2, 2, 2));

    final JPanel primaryOuterPanel = new JPanel(new BorderLayout());
    primaryOuterPanel.setBorder(new TitledBorder("Primary Call Graph"));

    final JPanel primaryPanel = new JPanel(new GridLayout(6, 1, 2, 2));
    primaryPanel.setBorder(new LineBorder(Color.GRAY));
    primaryPanel.setBackground(Color.WHITE);
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Image Name", TEXTFIELDLABELWIDTH, primaryName, TEXTFIELDHEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Functions", TEXTFIELDLABELWIDTH, primaryFunctions, TEXTFIELDHEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Calls", TEXTFIELDLABELWIDTH, primaryCalls, TEXTFIELDHEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Basic Blocks", TEXTFIELDLABELWIDTH, primaryBasicblocks, TEXTFIELDHEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Jumps", TEXTFIELDLABELWIDTH, primaryJumps, TEXTFIELDHEIGHT));
    primaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Instructions", TEXTFIELDLABELWIDTH, primaryInstructions, TEXTFIELDHEIGHT));

    primaryOuterPanel.add(primaryPanel, BorderLayout.CENTER);

    final JPanel secondaryOuterPanel = new JPanel(new BorderLayout());
    secondaryOuterPanel.setBorder(new TitledBorder("Secondary Call Graph"));

    final JPanel secondaryPanel = new JPanel(new GridLayout(6, 1, 2, 2));
    secondaryPanel.setBorder(new LineBorder(Color.GRAY));
    secondaryPanel.setBackground(Color.WHITE);
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Image Name", TEXTFIELDLABELWIDTH, secondaryName, TEXTFIELDHEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Functions", TEXTFIELDLABELWIDTH, secondaryFunctions, TEXTFIELDHEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Calls", TEXTFIELDLABELWIDTH, secondaryCalls, TEXTFIELDHEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Basic Blocks", TEXTFIELDLABELWIDTH, secondaryBasicblocks, TEXTFIELDHEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Jumps", TEXTFIELDLABELWIDTH, secondaryJumps, TEXTFIELDHEIGHT));
    secondaryPanel.add(
        GuiUtils.createHorizontalNamedLabelPanel(
            "Instructions", TEXTFIELDLABELWIDTH, secondaryInstructions, TEXTFIELDHEIGHT));

    secondaryOuterPanel.add(secondaryPanel, BorderLayout.CENTER);

    infoPanel.add(primaryOuterPanel);
    infoPanel.add(secondaryOuterPanel);

    return infoPanel;
  }

  private JPanel createTablePanel() {
    callgraphTable.setPreferredSize(new Dimension(callgraphTable.getPreferredSize().width, 40));

    final JScrollPane callgraphScrollpane = new JScrollPane(callgraphTable);
    callgraphScrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    final JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.setBorder(new TitledBorder(String.format("%d Call Graph View", 1)));

    tablePanel.add(callgraphScrollpane, BorderLayout.CENTER);
    tablePanel.setPreferredSize(new Dimension(tablePanel.getPreferredSize().width, 60));

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
    diff.getMetaData().removeListener(countsChangeListener);
  }

  @Override
  public List<AbstractTable> getTables() {
    final List<AbstractTable> list = new ArrayList<>();
    list.add(callgraphTable);
    return list;
  }

  private class InternalFlowgraphCachedCountsListener extends DiffChangeAdapter {
    @Override
    public void basicblocksCountChanged() {
      final MatchData matches = diff.getMatches();

      final int matchedBasicblocks = matches.getSizeOfMatchedBasicblocks();
      final int primaryUnmatchedBasicblocks = matches.getSizeOfUnmatchedBasicblocks(ESide.PRIMARY);
      final int secondaryUnmatchedBasicblocks =
          matches.getSizeOfUnmatchedBasicblocks(ESide.SECONDARY);

      primaryBasicblocks.updateData(matchedBasicblocks, primaryUnmatchedBasicblocks);
      secondaryBasicblocks.updateData(matchedBasicblocks, secondaryUnmatchedBasicblocks);
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
