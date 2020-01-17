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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup.CallGraphViewsPopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.BackgroundCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.PercentageThreeBarCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.SimilarityConfidenceCellRenderer;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Font;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class CallGraphViewTable extends AbstractTable {
  public CallGraphViewTable(
      final AbstractTableModel model, final WorkspaceTabPanelFunctions controller) {
    super(model, controller);

    final TableColumnModel colModel = getColumnModel();
    final TableColumn primaryName = colModel.getColumn(CallGraphViewTableModel.PRIMARY_NAME);
    final TableColumn functions = colModel.getColumn(CallGraphViewTableModel.FUNCTIONS);
    final TableColumn similarity = colModel.getColumn(CallGraphViewTableModel.SIMILARITY);
    final TableColumn confidence = colModel.getColumn(CallGraphViewTableModel.CONFIDENCE);
    final TableColumn calls = colModel.getColumn(CallGraphViewTableModel.CALLS);
    final TableColumn secondaryName = colModel.getColumn(CallGraphViewTableModel.SECONDARY_NAME);

    primaryName.setMinWidth(60);
    functions.setMinWidth(75);
    similarity.setMinWidth(40);
    confidence.setMinWidth(40);
    calls.setMinWidth(75);
    secondaryName.setMinWidth(60);

    primaryName.setPreferredWidth(200);
    functions.setPreferredWidth(75);
    similarity.setPreferredWidth(60);
    confidence.setPreferredWidth(60);
    calls.setPreferredWidth(75);
    secondaryName.setPreferredWidth(200);

    setRowHeight(GuiHelper.getMonospacedFontMetrics().getHeight() + 4);

    final SimilarityConfidenceCellRenderer similarityConfidenceRenderer =
        new SimilarityConfidenceCellRenderer();
    similarity.setCellRenderer(similarityConfidenceRenderer);
    confidence.setCellRenderer(similarityConfidenceRenderer);

    final Font monospacedFont = GuiHelper.getMonospacedFont();
    final BackgroundCellRenderer primaryBackgroundRenderer =
        new BackgroundCellRenderer(
            monospacedFont,
            Colors.TABLE_CELL_PRIMARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    primaryName.setCellRenderer(primaryBackgroundRenderer);

    final BackgroundCellRenderer secondaryBackgroundRenderer =
        new BackgroundCellRenderer(
            monospacedFont,
            Colors.TABLE_CELL_SECONDARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    secondaryName.setCellRenderer(secondaryBackgroundRenderer);

    final PercentageThreeBarCellRenderer matchesRenderer =
        new PercentageThreeBarCellRenderer(
            Colors.TABLE_CELL_PRIMARY_UNMATCHED_BACKGROUND,
            Colors.TABLE_CELL_MATCHED_BACKGROUND,
            Colors.TABLE_CELL_SECONDARY_UNMATCHED_BACKGROUND,
            Colors.TABLE_CELL_MATCHED_BACKGROUND,
            Colors.GRAY32);
    functions.setCellRenderer(matchesRenderer);
    calls.setCellRenderer(matchesRenderer);
  }

  @Override
  protected JPopupMenu getPopupMenu(final int rowIndex, final int columnIndex) {
    return new CallGraphViewsPopupMenu(this, columnIndex);
  }

  @Override
  protected void handleDoubleClick(final int row) {
    final WorkspaceTabPanelFunctions controller = getController();
    controller.openCallgraphView(controller.getMainWindow(), getDiff());
  }
}
