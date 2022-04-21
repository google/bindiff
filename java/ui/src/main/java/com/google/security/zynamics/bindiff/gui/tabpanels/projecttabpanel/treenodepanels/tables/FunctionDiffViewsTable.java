// Copyright 2011-2022 Google LLC
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
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup.FunctionDiffFlowGraphsViewTablePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.BackgroundCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.PercentageThreeBarCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.SimilarityConfidenceCellRenderer;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Font;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class FunctionDiffViewsTable extends AbstractTable {
  public FunctionDiffViewsTable(
      final AbstractTableModel model, final WorkspaceTabPanelFunctions controller) {
    super(model, controller);

    init();
  }

  private void init() {
    final TableColumnModel model = getColumnModel();
    final TableColumn viewName = model.getColumn(FunctionDiffViewsTableModel.VIEWNAME);
    final TableColumn primaryAddr = model.getColumn(FunctionDiffViewsTableModel.PRIMARY_ADDRESS);
    final TableColumn primaryName = model.getColumn(FunctionDiffViewsTableModel.PRIMARY_NAME);
    final TableColumn basicBlockMatches =
        model.getColumn(FunctionDiffViewsTableModel.BASICBLOCK_MATCHES);
    final TableColumn similarity = model.getColumn(FunctionDiffViewsTableModel.SIMILARITY);
    final TableColumn confidence = model.getColumn(FunctionDiffViewsTableModel.CONFIDENCE);
    final TableColumn jumpMatches = model.getColumn(FunctionDiffViewsTableModel.JUMP_MATCHES);
    final TableColumn secondaryName = model.getColumn(FunctionDiffViewsTableModel.SECONDARY_NAME);
    final TableColumn secondaryAddr =
        model.getColumn(FunctionDiffViewsTableModel.SECONDARY_ADDRESS);

    viewName.setMinWidth(80);
    primaryAddr.setMinWidth(68);
    primaryName.setMinWidth(50);
    basicBlockMatches.setMinWidth(75);
    similarity.setMinWidth(40);
    confidence.setMinWidth(40);
    jumpMatches.setMinWidth(75);
    secondaryName.setMinWidth(50);
    secondaryAddr.setMinWidth(68);

    viewName.setPreferredWidth(300);
    primaryAddr.setPreferredWidth(60);
    primaryName.setPreferredWidth(150);
    basicBlockMatches.setPreferredWidth(75);
    similarity.setPreferredWidth(60);
    confidence.setPreferredWidth(60);
    jumpMatches.setPreferredWidth(75);
    secondaryName.setPreferredWidth(150);
    secondaryAddr.setPreferredWidth(60);

    setRowHeight(GuiHelper.getMonospacedFontMetrics().getHeight() + 4);

    final SimilarityConfidenceCellRenderer similarityConfidenceRenderer =
        new SimilarityConfidenceCellRenderer();
    similarity.setCellRenderer(similarityConfidenceRenderer);
    confidence.setCellRenderer(similarityConfidenceRenderer);

    final Font monospacedFont = GuiHelper.getMonospacedFont();
    final BackgroundCellRenderer monospacedTextRenderer =
        new BackgroundCellRenderer(
            monospacedFont, Colors.GRAY250, Colors.GRAY32, SwingConstants.LEFT);
    viewName.setCellRenderer(monospacedTextRenderer);

    final BackgroundCellRenderer primaryBackgroundRenderer =
        new BackgroundCellRenderer(
            monospacedFont,
            Colors.TABLE_CELL_PRIMARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    primaryAddr.setCellRenderer(primaryBackgroundRenderer);
    primaryName.setCellRenderer(primaryBackgroundRenderer);

    final BackgroundCellRenderer secondaryBackgroundRenderer =
        new BackgroundCellRenderer(
            monospacedFont,
            Colors.TABLE_CELL_SECONDARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    secondaryAddr.setCellRenderer(secondaryBackgroundRenderer);
    secondaryName.setCellRenderer(secondaryBackgroundRenderer);

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
    return new FunctionDiffFlowGraphsViewTablePopupMenu(this, rowIndex, columnIndex);
  }

  @Override
  protected void handleDoubleClick(final int row) {
    final Diff diff = ((FunctionDiffViewsTableModel) getTableModel()).getDiffAt(row);
    final WorkspaceTabPanelFunctions controller = getController();
    controller.openFunctionDiffView((MainWindow) SwingUtilities.getWindowAncestor(this), diff);
  }

  public void addRow(final Diff diff) {
    ((FunctionDiffViewsTableModel) getTableModel()).addRow(diff);
    updateUI();
  }

  public void removeRow(final Diff diff) {
    ((FunctionDiffViewsTableModel) getTableModel()).removeRow(diff);
    updateUI();
  }
}
