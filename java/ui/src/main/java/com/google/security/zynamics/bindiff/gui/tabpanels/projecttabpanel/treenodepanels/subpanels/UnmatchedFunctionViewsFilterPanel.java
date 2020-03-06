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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.subpanels;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.components.viewsearchfield.IViewSearchFieldListener;
import com.google.security.zynamics.bindiff.gui.components.viewsearchfield.TableTextSearchComboBox;
import com.google.security.zynamics.bindiff.gui.dialogs.ViewSearchOptionsDialog;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.UnmatchedFunctionViewsTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.UnmatchedFunctionViewsTableModel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.helpers.GraphGetter;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.utils.ImageUtils;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.tables.CTableSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class UnmatchedFunctionViewsFilterPanel extends JPanel {
  private static final ImageIcon ICON_OPTIONS =
      ImageUtils.getImageIcon("data/buttonicons/options.png");
  private static final ImageIcon ICON_CLEAR = ImageUtils.getImageIcon("data/buttonicons/clear.png");
  private static final ImageIcon ICON_CLEAR_GRAY =
      ImageUtils.getImageIcon("data/buttonicons/clear-gray.png");

  private final InternalClearButtonLister clearButtonListener = new InternalClearButtonLister();
  private final InternalOptionButtonListener optionsButtonListener =
      new InternalOptionButtonListener();
  private final InternalSearchResultListener searchResultListener =
      new InternalSearchResultListener();

  private final TableTextSearchComboBox searchCombobox;

  private final JButton optionsButton = new JButton(ICON_OPTIONS);
  private final JButton clearButton = new JButton(ICON_CLEAR_GRAY);

  private final UnmatchedFunctionViewsTable unmatchedFunctionsViewTable;

  private final ESide side;

  public UnmatchedFunctionViewsFilterPanel(
      final UnmatchedFunctionViewsTable table, final ESide side) {
    super(new BorderLayout());

    unmatchedFunctionsViewTable = checkNotNull(table);
    this.side = checkNotNull(side);

    searchCombobox = new TableTextSearchComboBox(table, getColumnIndices(side));
    searchCombobox.addListener(searchResultListener);

    clearButton.addActionListener(clearButtonListener);
    optionsButton.addActionListener(optionsButtonListener);

    clearButton.setToolTipText("Clear Search Results");
    optionsButton.setToolTipText("Search Settings");

    add(createPanel(), BorderLayout.WEST);
  }

  private static List<Pair<Integer, ESide>> getColumnIndices(final ESide side) {
    final List<Pair<Integer, ESide>> indices = new ArrayList<>();

    indices.add(new Pair<>(UnmatchedFunctionViewsTableModel.ADDRESS, side));
    indices.add(new Pair<>(UnmatchedFunctionViewsTableModel.FUNCTION_NAME, side));
    indices.add(new Pair<>(UnmatchedFunctionViewsTableModel.TYPE, side));

    return indices;
  }

  private JPanel createPanel() {
    searchCombobox.setBackground(Color.WHITE);
    searchCombobox.setMinimumSize(new Dimension(30, 0));
    searchCombobox.setMaximumSize(new Dimension(400, 0));
    final Dimension size = new Dimension(400, searchCombobox.getPreferredSize().height - 4);

    searchCombobox.setPreferredSize(size);
    searchCombobox.setSize(size);

    clearButton.setBackground(Color.WHITE);
    clearButton.setFocusable(false);
    clearButton.setPreferredSize(new Dimension(32, 23));

    final JPanel searchBoxPanel = new JPanel(new BorderLayout());
    searchBoxPanel.add(searchCombobox, BorderLayout.CENTER);

    final JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(clearButton, BorderLayout.CENTER);

    optionsButton.setPreferredSize(new Dimension(32, 23));
    optionsButton.setBackground(Color.WHITE);
    optionsButton.setFocusable(false);
    clearButton.setPreferredSize(new Dimension(32, 23));
    clearButton.setBackground(Color.WHITE);
    clearButton.setFocusable(false);

    final JPanel outterButtonPanel = new JPanel(new GridLayout(1, 2, 1, 1));
    outterButtonPanel.setBorder(new EmptyBorder(0, 1, 0, 5));
    outterButtonPanel.add(clearButton);
    outterButtonPanel.add(optionsButton);

    buttonPanel.add(outterButtonPanel, BorderLayout.CENTER);
    searchBoxPanel.add(buttonPanel, BorderLayout.EAST);

    final JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.setMaximumSize(new Dimension(467, 23));
    searchPanel.setMinimumSize(new Dimension(467, 23));
    searchPanel.setSize(new Dimension(467, 23));
    searchPanel.add(searchBoxPanel, BorderLayout.WEST);

    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(0, 0, 5, 0));
    panel.add(searchPanel, BorderLayout.WEST);

    return panel;
  }

  public void dispose() {
    searchCombobox.removeListener(searchResultListener);
    clearButton.removeActionListener(clearButtonListener);
  }

  public TableTextSearchComboBox getSearchCombobox() {
    return searchCombobox;
  }

  private class InternalClearButtonLister implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent arg0) {
      searchCombobox.reset();
      clearButton.setIcon(ICON_CLEAR_GRAY);
    }
  }

  private class InternalOptionButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      final boolean isRegEx = searchCombobox.isRegEx();
      final boolean isCaseSensitive = searchCombobox.isCaseSensitive();
      final boolean isPrimarySide = ESide.PRIMARY == side;
      final boolean isSecondarySide = ESide.SECONDARY == side;
      final boolean isTempTableUse = searchCombobox.isTemporaryTableUse();

      ViewSearchOptionsDialog dlg;
      dlg =
          new ViewSearchOptionsDialog(
              SwingUtilities.getWindowAncestor(UnmatchedFunctionViewsFilterPanel.this),
              "Filter Options",
              isRegEx,
              isCaseSensitive,
              isPrimarySide,
              isSecondarySide,
              isTempTableUse);
      dlg.disableSideCheckboxes();

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
      final UnmatchedFunctionViewsTableModel tableModel =
          (UnmatchedFunctionViewsTableModel) unmatchedFunctionsViewTable.getTableModel();
      final Diff diff = tableModel.getDiff();

      final Set<RawFunction> filteredFunctions = new HashSet<>();
      filteredFunctions.addAll(GraphGetter.getUnmatchedFunctions(diff.getCallGraph(side)));

      tableModel.setUnmatchedFunctions(filteredFunctions);

      tableModel.fireTableDataChanged();

      clearButton.setIcon(ICON_CLEAR_GRAY);

      updateUI();
    }

    @Override
    public void searched(final List<Integer> rowIndices, final boolean selectResultsOnly) {
      final Set<RawFunction> functions = new HashSet<>();

      final UnmatchedFunctionViewsTableModel tableModel =
          (UnmatchedFunctionViewsTableModel) unmatchedFunctionsViewTable.getTableModel();

      final CTableSorter sorterModel = unmatchedFunctionsViewTable.getModel();

      final ListSelectionModel selectionModel = unmatchedFunctionsViewTable.getSelectionModel();

      for (final Integer index : rowIndices) {
        final int modelIndex = sorterModel.modelIndex(index);

        if (selectResultsOnly) {
          selectionModel.addSelectionInterval(index, index);
        } else {
          functions.add(tableModel.getUnmatchedFunctionAt(modelIndex));
        }
      }

      if (!selectResultsOnly) {
        tableModel.setUnmatchedFunctions(functions);

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
