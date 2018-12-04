package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup.CallGraphViewsPopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.BackgroundCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.PercentageThreeBarCellRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.SimilarityConfidenceCellRenderer;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.gui.GuiHelper;
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

    final SimilarityConfidenceCellRenderer similarityConfidenceRenderer =
        new SimilarityConfidenceCellRenderer();
    similarity.setCellRenderer(similarityConfidenceRenderer);
    confidence.setCellRenderer(similarityConfidenceRenderer);

    final BackgroundCellRenderer primaryBackgroundRenderer =
        new BackgroundCellRenderer(
            GuiHelper.getMonospacedFont(),
            Colors.TABLE_CELL_PRIMARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    primaryName.setCellRenderer(primaryBackgroundRenderer);

    final BackgroundCellRenderer secondaryBackgroundRenderer =
        new BackgroundCellRenderer(
            GuiHelper.getMonospacedFont(),
            Colors.TABLE_CELL_SECONDARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    secondaryName.setCellRenderer(secondaryBackgroundRenderer);

    final PercentageThreeBarCellRenderer functionCellRenderer =
        new PercentageThreeBarCellRenderer(
            Colors.TABLE_CELL_PRIMARY_UNMATCHED_BACKGROUND,
            Colors.TABLE_CELL_MATCHED_BACKGROUND,
            Colors.TABLE_CELL_SECONDARY_UNMATCHED_BACKGROUND,
            Colors.TABLE_CELL_MATCHED_BACKGROUND,
            Colors.GRAY32);
    functions.setCellRenderer(functionCellRenderer);

    final PercentageThreeBarCellRenderer callsCellRenderer =
        new PercentageThreeBarCellRenderer(
            Colors.TABLE_CELL_PRIMARY_UNMATCHED_BACKGROUND,
            Colors.TABLE_CELL_MATCHED_BACKGROUND,
            Colors.TABLE_CELL_SECONDARY_UNMATCHED_BACKGROUND,
            Colors.TABLE_CELL_MATCHED_BACKGROUND,
            Colors.GRAY32);
    calls.setCellRenderer(callsCellRenderer);
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
