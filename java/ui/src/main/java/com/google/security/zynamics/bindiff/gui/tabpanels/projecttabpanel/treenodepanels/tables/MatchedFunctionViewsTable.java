// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.IconCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup.FlowGraphViewsTablePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.BackgroundCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.FunctionTypeCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.PercentageThreeBarCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.SimilarityConfidenceCellRenderer;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.tables.CTableSorter;
import java.awt.Font;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class MatchedFunctionViewsTable extends AbstractTable {
  private final ListenerProvider<IMatchedFunctionsViewsTableListener> listeners =
      new ListenerProvider<>();

  private final InternalSelectionListener selectionListener = new InternalSelectionListener();

  public MatchedFunctionViewsTable(
      final AbstractTableModel model, final WorkspaceTabPanelFunctions controller) {
    super(model, controller);

    sortColumn(MatchedFunctionsViewsTableModel.SIMILARITY, CTableSorter.DESCENDING);
    sortColumn(MatchedFunctionsViewsTableModel.PRIMARY_TYPE, CTableSorter.ASCENDING);
    sortColumn(MatchedFunctionsViewsTableModel.CONFIDENCE, CTableSorter.ASCENDING);
    sortColumn(MatchedFunctionsViewsTableModel.PRIMARY_ADDRESS, CTableSorter.ASCENDING);

    init();

    getSelectionModel().addListSelectionListener(selectionListener);
  }

  private void init() {
    final TableColumnModel model = getColumnModel();

    final TableColumn icon = model.getColumn(MatchedFunctionsViewsTableModel.ICON);
    final TableColumn primaryAddr =
        model.getColumn(MatchedFunctionsViewsTableModel.PRIMARY_ADDRESS);
    final TableColumn primaryName = model.getColumn(MatchedFunctionsViewsTableModel.PRIMARY_NAME);
    final TableColumn primaryType = model.getColumn(MatchedFunctionsViewsTableModel.PRIMARY_TYPE);
    final TableColumn basicBlockMatches =
        model.getColumn(MatchedFunctionsViewsTableModel.BASICBLOCK_MATCHES);
    final TableColumn similarity = model.getColumn(MatchedFunctionsViewsTableModel.SIMILARITY);
    final TableColumn confidence = model.getColumn(MatchedFunctionsViewsTableModel.CONFIDENCE);
    final TableColumn jumpMatches = model.getColumn(MatchedFunctionsViewsTableModel.JUMP_MATCHES);
    final TableColumn secondaryType =
        model.getColumn(MatchedFunctionsViewsTableModel.SECONDARY_TYPE);
    final TableColumn secondaryName =
        model.getColumn(MatchedFunctionsViewsTableModel.SECONDARY_NAME);
    final TableColumn secondaryAddr =
        model.getColumn(MatchedFunctionsViewsTableModel.SECONDARY_ADDRESS);

    icon.setMinWidth(18);
    primaryAddr.setMinWidth(68);
    primaryName.setMinWidth(55);
    primaryType.setMinWidth(35);
    basicBlockMatches.setMinWidth(75);
    similarity.setMinWidth(40);
    confidence.setMinWidth(40);
    jumpMatches.setMinWidth(75);
    secondaryType.setMinWidth(35);
    secondaryName.setMinWidth(55);
    secondaryAddr.setMinWidth(68);

    icon.setMaxWidth(18);
    icon.setPreferredWidth(18);
    primaryAddr.setPreferredWidth(60);
    primaryName.setPreferredWidth(200);
    primaryType.setPreferredWidth(35);
    basicBlockMatches.setPreferredWidth(75);
    similarity.setPreferredWidth(60);
    confidence.setPreferredWidth(60);
    jumpMatches.setPreferredWidth(75);
    secondaryType.setPreferredWidth(35);
    secondaryName.setPreferredWidth(200);
    secondaryAddr.setPreferredWidth(60);

    setRowHeight(GuiHelper.getMonospacedFontMetrics().getHeight() + 4);

    icon.setCellRenderer(new IconCellRenderer());

    final SimilarityConfidenceCellRenderer similarityConfidenceRenderer =
        new SimilarityConfidenceCellRenderer();
    similarity.setCellRenderer(similarityConfidenceRenderer);
    confidence.setCellRenderer(similarityConfidenceRenderer);

    final Font monospacedFont = GuiHelper.getMonospacedFont();
    final BackgroundCellRenderer monospacedTextRenderer =
        new BackgroundCellRenderer(
            monospacedFont, Colors.GRAY250, Colors.GRAY32, SwingConstants.LEFT);
    primaryAddr.setCellRenderer(monospacedTextRenderer);
    primaryName.setCellRenderer(monospacedTextRenderer);
    secondaryAddr.setCellRenderer(monospacedTextRenderer);
    secondaryName.setCellRenderer(monospacedTextRenderer);

    final FunctionTypeCellRenderer functionTypeRenderer = new FunctionTypeCellRenderer();
    primaryType.setCellRenderer(functionTypeRenderer);
    secondaryType.setCellRenderer(functionTypeRenderer);

    final PercentageThreeBarCellRenderer matchesRenderer =
        new PercentageThreeBarCellRenderer(
            Colors.TABLE_CELL_PRIMARY_UNMATCHED_BACKGROUND,
            Colors.TABLE_CELL_MATCHED_BACKGROUND,
            Colors.TABLE_CELL_SECONDARY_UNMATCHED_BACKGROUND,
            Colors.TABLE_CELL_MATCHED_BACKGROUND,
            Colors.GRAY32);

    basicBlockMatches.setCellRenderer(matchesRenderer);
    jumpMatches.setCellRenderer(matchesRenderer);
  }

  @Override
  protected JPopupMenu getPopupMenu(final int rowIndex, final int columnIndex) {
    return new FlowGraphViewsTablePopupMenu(this, rowIndex, columnIndex);
  }

  @Override
  protected void handleDoubleClick(final int row) {
    final TableModel m = getTableModel();
    final IAddress primaryAddr =
        new CAddress(
            (String) m.getValueAt(row, MatchedFunctionsViewsTableModel.PRIMARY_ADDRESS), 16);
    final IAddress secondaryAddr =
        new CAddress(
            (String) m.getValueAt(row, MatchedFunctionsViewsTableModel.SECONDARY_ADDRESS), 16);

    final WorkspaceTabPanelFunctions controller = getController();
    controller.openFlowGraphView(controller.getMainWindow(), getDiff(), primaryAddr, secondaryAddr);
  }

  public void addListener(final IMatchedFunctionsViewsTableListener listener) {
    listeners.addListener(listener);
  }

  @Override
  public void dispose() {
    getSelectionModel().removeListSelectionListener(selectionListener);
  }

  public void removeListener(final IMatchedFunctionsViewsTableListener listener) {
    listeners.removeListener(listener);
  }

  private class InternalSelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(final ListSelectionEvent e) {
      for (final IMatchedFunctionsViewsTableListener listener : listeners) {
        listener.rowSelectionChanged(MatchedFunctionViewsTable.this);
      }
    }
  }
}
