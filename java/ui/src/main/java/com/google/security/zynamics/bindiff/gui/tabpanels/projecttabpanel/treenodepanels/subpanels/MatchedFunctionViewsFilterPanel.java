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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.subpanels;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.components.viewsearchfield.IViewSearchFieldListener;
import com.google.security.zynamics.bindiff.gui.components.viewsearchfield.TableTextSearchComboBox;
import com.google.security.zynamics.bindiff.gui.dialogs.ViewSearchOptionsDialog;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.MatchedFunctionViewsTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.MatchedFunctionsViewsTableModel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.helpers.GraphGetter;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.tables.CTableSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class MatchedFunctionViewsFilterPanel extends JPanel {
  private static final ImageIcon ICON_OPTIONS =
      ResourceUtils.getImageIcon("data/buttonicons/options.png");
  private static final ImageIcon ICON_CLEAR =
      ResourceUtils.getImageIcon("data/buttonicons/clear.png");
  private static final ImageIcon ICON_CLEAR_GRAY =
      ResourceUtils.getImageIcon("data/buttonicons/clear-gray.png");

  private final ListenerProvider<IViewsFilterCheckboxListener> checkBoxFilterListeners =
      new ListenerProvider<>();

  private final InternalSearchResultListener searchResultListener;
  private final InternalFilterCheckboxListener filterCheckboxListener;
  private final InternalClearButtonLister clearButtonListener = new InternalClearButtonLister();
  private final InternalOptionButtonListener optionsButtonListener;

  private final JButton optionsButton = new JButton(ICON_OPTIONS);
  private final JButton clearButton = new JButton(ICON_CLEAR_GRAY);

  private final JCheckBox showStructuralChangedFunctions =
      new JCheckBox("Show structural changes", true);
  private final JCheckBox showInstructionChangedOnlyFunctions =
      new JCheckBox("Show only instructions changed", true);
  private final JCheckBox showIdenticalFunctions = new JCheckBox("Show identical", true);

  private final TableTextSearchComboBox searchCombobox;

  private final MatchedFunctionViewsTable matchedFunctionsViewTable;

  public MatchedFunctionViewsFilterPanel(final MatchedFunctionViewsTable table) {
    super(new BorderLayout());

    checkNotNull(table);

    searchResultListener = new InternalSearchResultListener();
    filterCheckboxListener = new InternalFilterCheckboxListener();
    optionsButtonListener = new InternalOptionButtonListener();

    matchedFunctionsViewTable = table;

    searchCombobox = new TableTextSearchComboBox(table, getColumnIndices());
    searchCombobox.addListener(searchResultListener);

    clearButton.addActionListener(clearButtonListener);
    optionsButton.addActionListener(optionsButtonListener);

    clearButton.setToolTipText("Clear Search Results");
    optionsButton.setToolTipText("Search Settings");

    showStructuralChangedFunctions.addItemListener(filterCheckboxListener);
    showInstructionChangedOnlyFunctions.addItemListener(filterCheckboxListener);
    showIdenticalFunctions.addItemListener(filterCheckboxListener);

    searchCombobox.setSearchOptions(false, false, true, true, false);

    add(createPanel(), BorderLayout.CENTER);
  }

  private static List<Pair<Integer, ESide>> getColumnIndices() {
    final List<Pair<Integer, ESide>> indices = new ArrayList<>();

    indices.add(new Pair<>(MatchedFunctionsViewsTableModel.PRIMARY_ADDRESS, ESide.PRIMARY));
    indices.add(new Pair<>(MatchedFunctionsViewsTableModel.PRIMARY_NAME, ESide.PRIMARY));
    indices.add(new Pair<>(MatchedFunctionsViewsTableModel.PRIMARY_TYPE, ESide.PRIMARY));
    indices.add(new Pair<>(MatchedFunctionsViewsTableModel.SECONDARY_ADDRESS, ESide.SECONDARY));
    indices.add(new Pair<>(MatchedFunctionsViewsTableModel.SECONDARY_NAME, ESide.SECONDARY));
    indices.add(new Pair<>(MatchedFunctionsViewsTableModel.SECONDARY_TYPE, ESide.SECONDARY));

    return indices;
  }

  private JPanel createChangesFilterPanel() {
    showStructuralChangedFunctions.setMinimumSize(new Dimension(0, 0));
    showInstructionChangedOnlyFunctions.setMinimumSize(new Dimension(0, 0));
    showIdenticalFunctions.setMinimumSize(new Dimension(0, 0));

    final JPanel p1 = new JPanel(new BorderLayout());
    p1.add(showStructuralChangedFunctions, BorderLayout.WEST);

    final JPanel p2 = new JPanel(new BorderLayout());
    p2.add(showInstructionChangedOnlyFunctions, BorderLayout.WEST);

    p1.add(p2, BorderLayout.CENTER);

    final JPanel p3 = new JPanel(new BorderLayout());
    p3.add(showIdenticalFunctions, BorderLayout.WEST);

    p2.add(p3, BorderLayout.CENTER);

    return p1;
  }

  private JPanel createPanel() {
    searchCombobox.setBackground(Color.WHITE);
    searchCombobox.setMinimumSize(new Dimension(30, 0));
    final Dimension size =
        new Dimension(
            searchCombobox.getPreferredSize().width, searchCombobox.getPreferredSize().height - 4);
    searchCombobox.setPreferredSize(size);
    searchCombobox.setSize(size);

    final JPanel searchBoxPanel = new JPanel(new BorderLayout());
    searchBoxPanel.add(searchCombobox, BorderLayout.CENTER);

    final JPanel filterPanel = new JPanel(new BorderLayout());
    filterPanel.add(createTextFilterPanel(), BorderLayout.WEST);

    final JPanel changesPanel = new JPanel(new BorderLayout());
    changesPanel.add(createChangesFilterPanel(), BorderLayout.WEST);
    changesPanel.setBorder(new EmptyBorder(0, 5, 0, 1));

    searchBoxPanel.add(filterPanel, BorderLayout.EAST);
    filterPanel.add(changesPanel, BorderLayout.CENTER);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(0, 0, 5, 0));
    panel.add(searchBoxPanel);

    return panel;
  }

  private JPanel createTextFilterPanel() {
    final JPanel buttonPanel = new JPanel(new BorderLayout());
    final Dimension dim = searchCombobox.getPreferredSize();

    optionsButton.setPreferredSize(new Dimension(32, dim.height));
    optionsButton.setBackground(Color.WHITE);
    optionsButton.setFocusable(false);
    clearButton.setPreferredSize(new Dimension(32, dim.height));
    clearButton.setBackground(Color.WHITE);
    clearButton.setFocusable(false);

    final JPanel outterButtonPanel = new JPanel(new GridLayout(1, 2, 1, 1));
    outterButtonPanel.setBorder(new EmptyBorder(0, 1, 0, 0));
    outterButtonPanel.add(clearButton);
    outterButtonPanel.add(optionsButton);

    buttonPanel.add(outterButtonPanel, BorderLayout.CENTER);

    return buttonPanel;
  }

  public void addListener(final IViewsFilterCheckboxListener listener) {
    checkBoxFilterListeners.addListener(listener);
  }

  public void dispose() {
    searchCombobox.removeListener(searchResultListener);

    clearButton.removeActionListener(clearButtonListener);
    optionsButton.removeActionListener(optionsButtonListener);

    showStructuralChangedFunctions.removeItemListener(filterCheckboxListener);
    showInstructionChangedOnlyFunctions.removeItemListener(filterCheckboxListener);
    showIdenticalFunctions.removeItemListener(filterCheckboxListener);
  }

  public void removeListener(final IViewsFilterCheckboxListener listener) {
    checkBoxFilterListeners.removeListener(listener);
  }

  private class InternalClearButtonLister implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent arg0) {
      searchCombobox.reset();
      clearButton.setIcon(ICON_CLEAR_GRAY);
    }
  }

  private class InternalFilterCheckboxListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      final boolean structuralChange = showStructuralChangedFunctions.isSelected();
      final boolean instructionOnlyChange = showInstructionChangedOnlyFunctions.isSelected();
      final boolean identical = showIdenticalFunctions.isSelected();

      for (final IViewsFilterCheckboxListener listener : checkBoxFilterListeners) {
        listener.functionViewsFilterChanged(structuralChange, instructionOnlyChange, identical);
      }

      clearButton.setIcon(ICON_CLEAR_GRAY);
      updateUI();
    }
  }

  private class InternalOptionButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      final boolean isRegEx = searchCombobox.isRegEx();
      final boolean isCaseSensitive = searchCombobox.isCaseSensitive();
      final boolean isPrimarySide = searchCombobox.isPrimarySideSearch();
      final boolean isSecondarySide = searchCombobox.isSecondarySideSearch();
      final boolean isTempTableUse = searchCombobox.isTemporaryTableUse();

      ViewSearchOptionsDialog dlg;
      dlg =
          new ViewSearchOptionsDialog(
              SwingUtilities.getWindowAncestor(MatchedFunctionViewsFilterPanel.this),
              "Filter Options",
              isRegEx,
              isCaseSensitive,
              isPrimarySide,
              isSecondarySide,
              isTempTableUse);

      dlg.setVisible(true);

      if (dlg.getOkButtonPressed()) {
        if (isRegEx != dlg.getRegExSelected()
            || isCaseSensitive != dlg.getCaseSensitiveSelected()
            || isPrimarySide != dlg.getPrimarySideSearch()
            || isSecondarySide != dlg.getSecondarySideSearch()
            || isTempTableUse != dlg.getTemporaryTableUse()) {
          searchCombobox.setSearchOptions(
              dlg.getRegExSelected(),
              dlg.getCaseSensitiveSelected(),
              dlg.getPrimarySideSearch(),
              dlg.getSecondarySideSearch(),
              dlg.getTemporaryTableUse());
          searchCombobox.updateResults();
        }
      }
    }
  }

  private class InternalSearchResultListener implements IViewSearchFieldListener {
    @Override
    public void reset() {
      final MatchedFunctionsViewsTableModel tableModel =
          (MatchedFunctionsViewsTableModel) matchedFunctionsViewTable.getTableModel();

      final Diff diff = tableModel.getDiff();
      final RawCallGraph priCallgraph = diff.getCallGraph(ESide.PRIMARY);
      final RawCallGraph secCallgraph = diff.getCallGraph(ESide.SECONDARY);

      final Set<Pair<RawFunction, RawFunction>> filteredFunctions = new HashSet<>();

      if (showStructuralChangedFunctions.isSelected()
          && showInstructionChangedOnlyFunctions.isSelected()
          && showIdenticalFunctions.isSelected()) {
        filteredFunctions.addAll(GraphGetter.getMatchedFunctionPairs(priCallgraph, secCallgraph));
      } else {
        if (showStructuralChangedFunctions.isSelected()) {
          filteredFunctions.addAll(
              GraphGetter.getStructuralChangedFunctionPairs(priCallgraph, secCallgraph));
        }

        if (showInstructionChangedOnlyFunctions.isSelected()) {
          filteredFunctions.addAll(
              GraphGetter.getInstructionOnlyChangedFunctionPairs(priCallgraph, secCallgraph));
        }

        if (showIdenticalFunctions.isSelected()) {
          filteredFunctions.addAll(
              GraphGetter.getIdenticalFunctionPairs(priCallgraph, secCallgraph));
        }
      }

      tableModel.setMatchedFunctionPairs(filteredFunctions);

      tableModel.fireTableDataChanged();

      clearButton.setIcon(ICON_CLEAR_GRAY);

      updateUI();
    }

    @Override
    public void searched(final List<Integer> rowIndices, final boolean selectResultsOnly) {
      final Set<Pair<RawFunction, RawFunction>> functionPairs = new HashSet<>();

      final MatchedFunctionsViewsTableModel tableModel =
          (MatchedFunctionsViewsTableModel) matchedFunctionsViewTable.getTableModel();

      final CTableSorter sorterModel = matchedFunctionsViewTable.getModel();

      final ListSelectionModel selectionModel = matchedFunctionsViewTable.getSelectionModel();

      for (final Integer index : rowIndices) {
        final int modelIndex = sorterModel.modelIndex(index);

        if (selectResultsOnly) {
          selectionModel.addSelectionInterval(index, index);
        } else {
          functionPairs.add(tableModel.getMatchedFunctionPairAt(modelIndex));
        }
      }

      if (!selectResultsOnly) {
        tableModel.setMatchedFunctionPairs(functionPairs);
        tableModel.fireTableDataChanged();
      }

      if (rowIndices.size() > 0) {
        clearButton.setIcon(ICON_CLEAR);
      } else {
        clearButton.setIcon(ICON_CLEAR_GRAY);
      }

      updateUI();
    }
  }
}
