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

package com.google.security.zynamics.bindiff.gui.dialogs.searchresultsdialog;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.searchers.SearchResult;
import com.google.security.zynamics.bindiff.gui.components.graphsearchfield.GraphSearchField;
import com.google.security.zynamics.bindiff.gui.components.graphsearchfield.IGraphSearchFieldListener;
import com.google.security.zynamics.bindiff.gui.dialogs.BaseDialog;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class SearchResultsDialog extends BaseDialog {
  private final JTabbedPane tabbedPane = new JTabbedPane();

  private final JTable primaryTable = new JTable();
  private final JTable secondaryTable = new JTable();

  private final JLabel primaryResultsLabel = new JLabel("");
  private final JLabel secondaryResultsLabel = new JLabel("");

  private final InternalTableListener listener = new InternalTableListener();

  private final IGraphSearchFieldListener searchListener = new InternalSearchListener();

  private final GraphSearchField searchField;

  private boolean wasVisible = false;

  public SearchResultsDialog(final Window parent, final GraphSearchField searchField) {
    super(parent, "Search Results");
    setModal(false);

    this.searchField = searchField;
    setResults(searchField.getGraphs());

    setLayout(new BorderLayout());

    searchField.addListener(searchListener);

    primaryTable.setDefaultRenderer(Object.class, new SearchResultsCellRenderer());
    primaryTable.getSelectionModel().addListSelectionListener(listener);

    primaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    secondaryTable.setDefaultRenderer(Object.class, new SearchResultsCellRenderer());
    secondaryTable.getSelectionModel().addListSelectionListener(listener);

    secondaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    init();

    setSize(400, 400);
  }

  private void fillUpTableData(final List<SearchResult> searchResults, final Object[][] tableData) {
    int counter = 0;

    Color color = null;
    Object lastObject = null;

    for (final SearchResult result : searchResults) {
      if (lastObject == null || lastObject != result.getObject()) {
        lastObject = result.getObject();

        if (color == null || color.equals(Color.WHITE)) {
          color = new Color(242, 242, 242);
        } else {
          color = Color.WHITE;
        }
      }

      result.setObjectMarkerColor(color);

      if (result.getObject() instanceof ZyGraphNode<?>) {
        tableData[counter++][0] = result;
      } else if (result.getObject() instanceof ZyGraphEdge<?, ?, ?>) {
        tableData[counter++][0] = result;
      }
    }
  }

  private void removAllRows(final JTable table) {
    final DefaultTableModel dm = (DefaultTableModel) table.getModel();
    dm.getDataVector().removeAllElements();
  }

  private void setResults(final GraphsContainer graphs) {
    final List<SearchResult> primaryResults = new ArrayList<>();
    primaryResults.addAll(graphs.getPrimaryGraph().getGraphSearcher().getSubObjectResults());

    final List<SearchResult> secondaryResults = new ArrayList<>();
    secondaryResults.addAll(graphs.getSecondaryGraph().getGraphSearcher().getSubObjectResults());

    final Object[][] primaryData = new Object[primaryResults.size()][1];
    final Object[][] secondaryData = new Object[secondaryResults.size()][1];

    fillUpTableData(primaryResults, primaryData);
    fillUpTableData(secondaryResults, secondaryData);

    primaryTable.setModel(new CResultsTableModel(primaryData, new String[] {"Primary Results"}));
    secondaryTable.setModel(
        new CResultsTableModel(secondaryData, new String[] {"Secondary Results"}));

    primaryResultsLabel.setText(String.format("%d search results", primaryData.length));
    secondaryResultsLabel.setText(String.format("%d search results", secondaryData.length));
  }

  private void init() {
    tabbedPane.setFocusable(false);

    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(tabbedPane, BorderLayout.CENTER);

    final JPanel primaryScrollPanel = new JPanel(new BorderLayout());
    primaryScrollPanel.setBorder(new EmptyBorder(2, 2, 2, 1));
    primaryScrollPanel.add(new JScrollPane(primaryTable), BorderLayout.CENTER);

    final JPanel secondaryScrollPanel = new JPanel(new BorderLayout());
    secondaryScrollPanel.setBorder(new EmptyBorder(2, 2, 2, 1));
    secondaryScrollPanel.add(new JScrollPane(secondaryTable), BorderLayout.CENTER);

    final JPanel primaryPanel = new JPanel(new BorderLayout());
    primaryPanel.setBorder(new LineBorder(Color.GRAY));
    primaryPanel.add(primaryScrollPanel, BorderLayout.CENTER);
    primaryResultsLabel.setBorder(new EmptyBorder(2, 2, 5, 2));
    primaryPanel.add(primaryResultsLabel, BorderLayout.SOUTH);

    tabbedPane.add("Primary", primaryPanel);

    final JPanel secondaryPanel = new JPanel(new BorderLayout());
    secondaryPanel.setBorder(new LineBorder(Color.GRAY));
    secondaryPanel.add(secondaryScrollPanel, BorderLayout.CENTER);
    secondaryResultsLabel.setBorder(new EmptyBorder(2, 2, 5, 1));
    secondaryPanel.add(secondaryResultsLabel, BorderLayout.SOUTH);

    tabbedPane.add("Secondary", secondaryPanel);

    add(mainPanel, BorderLayout.CENTER);
  }

  public boolean getReshowDialog() {
    return wasVisible;
  }

  public void setReshowDialog(final boolean unhide) {
    wasVisible = unhide;
  }

  private class CResultsTableModel extends DefaultTableModel {
    public CResultsTableModel(final Object[][] data, final String[] strings) {
      super(data, strings);
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
      return false;
    }
  }

  private class InternalSearchListener implements IGraphSearchFieldListener {
    @Override
    public void cleaned() {
      searched();
    }

    @Override
    public void searched() {
      removAllRows(primaryTable);
      removAllRows(secondaryTable);

      setResults(searchField.getGraphs());

      primaryTable.updateUI();
      secondaryTable.updateUI();

      primaryResultsLabel.updateUI();
      secondaryResultsLabel.updateUI();
    }
  }

  private class InternalTableListener implements ListSelectionListener {
    @Override
    public void valueChanged(final ListSelectionEvent event) {
      if (tabbedPane.getSelectedIndex() == 0) {
        final int selectedResult = primaryTable.getSelectedRow();

        if (selectedResult == -1) {
          return;
        }

        searchField.jumpToIndex(selectedResult, ESide.PRIMARY);
      } else {
        final int selectedResult = secondaryTable.getSelectedRow();

        if (selectedResult == -1) {
          return;
        }

        searchField.jumpToIndex(selectedResult, ESide.SECONDARY);
      }
    }
  }
}
