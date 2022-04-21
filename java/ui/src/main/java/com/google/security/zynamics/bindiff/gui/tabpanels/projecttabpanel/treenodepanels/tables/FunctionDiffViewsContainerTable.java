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
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Font;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

public class FunctionDiffViewsContainerTable extends AbstractTable {
  public FunctionDiffViewsContainerTable(
      final AbstractTableModel model, final WorkspaceTabPanelFunctions controller) {
    super(model, controller);

    init();
  }

  private void init() {
    final TableColumn priImageName =
        getColumnModel().getColumn(FunctionDiffViewsContainerTableModel.PRIMARY_IMAGE_NAME);
    final TableColumn priImageHash =
        getColumnModel().getColumn(FunctionDiffViewsContainerTableModel.PRIMARY_IMAGE_HASH);
    final TableColumn secImageName =
        getColumnModel().getColumn(FunctionDiffViewsContainerTableModel.SECONDARY_IMAGE_NAME);
    final TableColumn secImageHash =
        getColumnModel().getColumn(FunctionDiffViewsContainerTableModel.SECONDARY_IMAGE_HASH);
    final TableColumn viewName =
        getColumnModel().getColumn(FunctionDiffViewsContainerTableModel.VIEW_NAME);
    final TableColumn creationDate =
        getColumnModel().getColumn(FunctionDiffViewsContainerTableModel.CREATION_DATE);

    priImageName.setMinWidth(100);
    priImageHash.setMinWidth(100);
    secImageName.setMinWidth(100);
    secImageHash.setMinWidth(100);
    viewName.setMinWidth(200);
    creationDate.setMinWidth(120);

    priImageName.setPreferredWidth(100);
    priImageHash.setPreferredWidth(100);
    secImageName.setPreferredWidth(100);
    secImageHash.setPreferredWidth(100);
    viewName.setPreferredWidth(300);
    creationDate.setPreferredWidth(120);

    setRowHeight(GuiHelper.getMonospacedFontMetrics().getHeight() + 4);

    final Font monospacedFont = GuiHelper.getMonospacedFont();
    final BackgroundCellRenderer primaryBackgroundRenderer =
        new BackgroundCellRenderer(
            monospacedFont,
            Colors.TABLE_CELL_PRIMARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    priImageName.setCellRenderer(primaryBackgroundRenderer);
    priImageHash.setCellRenderer(primaryBackgroundRenderer);

    final BackgroundCellRenderer secondaryBackgroundRenderer =
        new BackgroundCellRenderer(
            monospacedFont,
            Colors.TABLE_CELL_SECONDARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    secImageName.setCellRenderer(secondaryBackgroundRenderer);
    secImageHash.setCellRenderer(secondaryBackgroundRenderer);

    final BackgroundCellRenderer textRenderer =
        new BackgroundCellRenderer(
            GuiHelper.getDefaultFont(), Colors.GRAY250, Colors.GRAY32, SwingConstants.LEFT);
    viewName.setCellRenderer(textRenderer);
    creationDate.setCellRenderer(textRenderer);
  }

  @Override
  protected JPopupMenu getPopupMenu(final int rowIndex, final int columnIndex) {
    return new FunctionDiffFlowGraphsViewTablePopupMenu(this, rowIndex, columnIndex);
  }

  @Override
  protected void handleDoubleClick(final int row) {
    final Diff diff = ((FunctionDiffViewsContainerTableModel) getTableModel()).getDiffAt(row);
    final WorkspaceTabPanelFunctions controller = getController();
    controller.openFunctionDiffView((MainWindow) SwingUtilities.getWindowAncestor(this), diff);
  }

  public void addRow(final Diff diff) {
    ((FunctionDiffViewsContainerTableModel) getTableModel()).addRow(diff);
    updateUI();
  }

  public void removeRow(final Diff diff) {
    ((FunctionDiffViewsContainerTableModel) getTableModel()).removeRow(diff);
    updateUI();
  }
}
