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

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.security.zynamics.bindiff.enums.ECallDirection;
import com.google.security.zynamics.bindiff.enums.EExistence;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.edges.SingleViewEdge;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.ExtendedMatchedFunctionViewsTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.ExtendedMatchedFunctionViewsTableModel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.IMatchedFunctionsViewsTableListener;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.MatchedFunctionViewsTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.MatchedFunctionsViewsTableModel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Triple;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class AddedAndRemovedCalledFunctionsPanel extends JPanel {
  private static final String INCOMING_CALLS_BORDER_TEXT =
      "+%d / -%d Added and removed Parent Functions calling the selected Functions";
  private static final String OUTGOING_CALLS_BORDER_TEXT =
      "+%d / -%d Added and removed Child Functions called from the selected Functions";

  private final TitledBorder incomingBorder =
      new TitledBorder(String.format(INCOMING_CALLS_BORDER_TEXT, 0, 0));
  private final TitledBorder outgoingBorder =
      new TitledBorder(String.format(OUTGOING_CALLS_BORDER_TEXT, 0, 0));

  private final ExtendedMatchedFunctionViewsTable incomingFunctionsCalledTable;
  private final ExtendedMatchedFunctionViewsTable outgoingFunctionsCalledTable;

  private final Diff diff;

  private final MatchedFunctionViewsTable matchedfunctionViewsTable;

  private final InternalMatchedFunctionViewsTableListener matchedFunctionViewsSelectionListener =
      new InternalMatchedFunctionViewsTableListener();

  public AddedAndRemovedCalledFunctionsPanel(
      final Diff diff,
      final WorkspaceTabPanelFunctions controller,
      final MatchedFunctionViewsTable matchedfunctionViewTable) {
    super(new BorderLayout());

    Preconditions.checkNotNull(diff);
    Preconditions.checkNotNull(controller);
    Preconditions.checkNotNull(matchedfunctionViewTable);

    this.diff = diff;
    matchedfunctionViewsTable = matchedfunctionViewTable;

    incomingFunctionsCalledTable =
        new ExtendedMatchedFunctionViewsTable(
            new ExtendedMatchedFunctionViewsTableModel(diff), controller);
    outgoingFunctionsCalledTable =
        new ExtendedMatchedFunctionViewsTable(
            new ExtendedMatchedFunctionViewsTableModel(diff), controller);

    matchedfunctionViewsTable.addListener(matchedFunctionViewsSelectionListener);
    add(createFunctionsCalledPanel(), BorderLayout.CENTER);
  }

  private JPanel createFunctionsCalledPanel() {
    final JPanel panel = new JPanel(new BorderLayout());

    final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
    splitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    splitPane.setRightComponent(createOutgoingFunctionsCalledTable());
    splitPane.setLeftComponent(createIncomingFunctionsCalledPanel());
    splitPane.setOneTouchExpandable(true);
    splitPane.setResizeWeight(.5);
    splitPane.setDividerLocation(.5);

    panel.add(splitPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createIncomingFunctionsCalledPanel() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(incomingBorder);
    panel.add(createTablePanel(incomingFunctionsCalledTable), BorderLayout.CENTER);

    return panel;
  }

  private JPanel createOutgoingFunctionsCalledTable() {
    final JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(outgoingBorder);
    panel.add(createTablePanel(outgoingFunctionsCalledTable), BorderLayout.CENTER);

    return panel;
  }

  private JPanel createTablePanel(final AbstractTable table) {
    final JPanel panel = new JPanel(new BorderLayout());
    final JScrollPane scrollPane = new JScrollPane(table);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private Multiset<RawFunction> getFunctions(
      final RawFunction function, final ECallDirection callDirection) {
    final Multiset<RawFunction> multiSet = HashMultiset.create();
    final List<SingleViewEdge<? extends SingleViewNode>> calls;
    calls =
        callDirection == ECallDirection.INCOMING
            ? function.getIncomingEdges()
            : function.getOutgoingEdges();

    for (final SingleViewEdge<? extends SingleViewNode> call : calls) {
      multiSet.add(
          callDirection == ECallDirection.INCOMING
              ? (RawFunction) call.getSource()
              : (RawFunction) call.getTarget());
    }

    return multiSet;
  }

  private void getSecondaryFunctionCallChanges(
      final IAddress primaryAddr,
      final IAddress secondaryAddr,
      final ECallDirection callDirection,
      final List<Triple<RawFunction, RawFunction, EExistence>> returnValue) {
    final RawFunction priStartFunction = diff.getCallGraph(ESide.PRIMARY).getFunction(primaryAddr);
    final RawFunction secStartFunction =
        diff.getCallGraph(ESide.SECONDARY).getFunction(secondaryAddr);

    final Multiset<RawFunction> priCalledFunctions = getFunctions(priStartFunction, callDirection);
    final Multiset<RawFunction> secCalledFunctions = getFunctions(secStartFunction, callDirection);

    final Collection<RawFunction> priRemoved = new ArrayList<>();
    final Collection<RawFunction> secToRemove = new ArrayList<>();
    for (final RawFunction secFunction : secCalledFunctions) {
      final RawFunction priFunction = secFunction.getMatchedFunction();
      if (priFunction != null && priCalledFunctions.remove(priFunction)) {
        priRemoved.add(priFunction);
        secToRemove.add(secFunction);
      }
    }
    secCalledFunctions.removeAll(secToRemove);

    for (final RawFunction secFunction : secCalledFunctions) {
      returnValue.add(Triple.make(secFunction.getMatchedFunction(), secFunction, EExistence.ADDED));
    }
    priCalledFunctions.addAll(priRemoved);
    secCalledFunctions.addAll(secToRemove);

    final Collection<RawFunction> priToRemove = new ArrayList<>();
    for (final RawFunction priFunction : priCalledFunctions) {
      final RawFunction secFunction = priFunction.getMatchedFunction();
      if (secFunction != null && secCalledFunctions.remove(secFunction)) {
        priToRemove.add(priFunction);
      }
    }
    priCalledFunctions.removeAll(priToRemove);

    for (final RawFunction priFunction : priCalledFunctions) {
      returnValue.add(
          Triple.make(priFunction, priFunction.getMatchedFunction(), EExistence.REMOVED));
    }
  }

  private void updateBorders(
      final int addedIncomingFunctionsCalledCount,
      final int removedIncomingFunctionsCalledCount,
      final int addedOutgoingFunctionsCalledCount,
      final int removedOutgoingFunctionsCalledCount) {
    incomingBorder.setTitle(
        String.format(
            INCOMING_CALLS_BORDER_TEXT,
            addedIncomingFunctionsCalledCount,
            removedIncomingFunctionsCalledCount));
    outgoingBorder.setTitle(
        String.format(
            OUTGOING_CALLS_BORDER_TEXT,
            addedOutgoingFunctionsCalledCount,
            removedOutgoingFunctionsCalledCount));

    updateUI();
  }

  public void dispose() {
    matchedfunctionViewsTable.removeListener(matchedFunctionViewsSelectionListener);
  }

  private class InternalMatchedFunctionViewsTableListener
      implements IMatchedFunctionsViewsTableListener {
    @Override
    public void rowSelectionChanged(final MatchedFunctionViewsTable table) {
      final int[] selectedRowIndices = table.getSelectedRows();

      final List<Triple<RawFunction, RawFunction, EExistence>> incomingFunctionsCalled =
          new ArrayList<>();
      final List<Triple<RawFunction, RawFunction, EExistence>> outgoingCalledFunctions =
          new ArrayList<>();

      int addedIncomingCount = 0;
      int removedIncomingCount = 0;
      int addedOutgoingCount = 0;
      int removedOutgoingCount = 0;

      for (final int row : selectedRowIndices) {
        final String primaryAddrString =
            (String)
                table.getModel().getValueAt(row, MatchedFunctionsViewsTableModel.PRIMARY_ADDRESS);
        final String secondaryAddrString =
            (String)
                table.getModel().getValueAt(row, MatchedFunctionsViewsTableModel.SECONDARY_ADDRESS);

        final IAddress primaryAddr = new CAddress(primaryAddrString, 16);
        final IAddress secondaryAddr = new CAddress(secondaryAddrString, 16);

        getSecondaryFunctionCallChanges(
            primaryAddr, secondaryAddr, ECallDirection.INCOMING, incomingFunctionsCalled);

        getSecondaryFunctionCallChanges(
            primaryAddr, secondaryAddr, ECallDirection.OUTGOING, outgoingCalledFunctions);
      }

      for (final Triple<RawFunction, RawFunction, EExistence> triple : incomingFunctionsCalled) {
        if (triple.third() == EExistence.ADDED) {
          ++addedIncomingCount;
          continue;
        }
        ++removedIncomingCount;
      }

      for (final Triple<RawFunction, RawFunction, EExistence> triple : outgoingCalledFunctions) {
        if (triple.third() == EExistence.ADDED) {
          ++addedOutgoingCount;
          continue;
        }
        ++removedOutgoingCount;
      }

      ((ExtendedMatchedFunctionViewsTableModel) outgoingFunctionsCalledTable.getTableModel())
          .setMatchedFunctionPairs(outgoingCalledFunctions);
      ((ExtendedMatchedFunctionViewsTableModel) incomingFunctionsCalledTable.getTableModel())
          .setMatchedFunctionPairs(incomingFunctionsCalled);
      ((ExtendedMatchedFunctionViewsTableModel) outgoingFunctionsCalledTable.getTableModel())
          .fireTableDataChanged();
      ((ExtendedMatchedFunctionViewsTableModel) incomingFunctionsCalledTable.getTableModel())
          .fireTableDataChanged();

      updateBorders(
          addedIncomingCount, removedIncomingCount, addedOutgoingCount, removedOutgoingCount);
    }

    @Override
    public void tableDataChanged(final MatchedFunctionsViewsTableModel model) {
      // do nothinhg
    }
  }
}
