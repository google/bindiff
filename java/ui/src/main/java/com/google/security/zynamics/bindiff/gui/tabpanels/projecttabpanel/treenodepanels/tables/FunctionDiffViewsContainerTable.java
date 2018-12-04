package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup.FunctionDiffFlowGraphsViewTablePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.BackgroundCellRenderer;
import com.google.security.zynamics.bindiff.gui.window.MainWindow;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.gui.GuiHelper;
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

    final BackgroundCellRenderer primaryBackgroundRenderer =
        new BackgroundCellRenderer(
            GuiHelper.getMonospacedFont(),
            Colors.TABLE_CELL_PRIMARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    priImageName.setCellRenderer(primaryBackgroundRenderer);
    priImageHash.setCellRenderer(primaryBackgroundRenderer);

    final BackgroundCellRenderer secondaryBackgroundRenderer =
        new BackgroundCellRenderer(
            GuiHelper.getMonospacedFont(),
            Colors.TABLE_CELL_SECONDARY_DEFAULT_BACKGROUND,
            Colors.GRAY32,
            SwingConstants.LEFT);
    secImageName.setCellRenderer(secondaryBackgroundRenderer);
    secImageHash.setCellRenderer(secondaryBackgroundRenderer);

    final BackgroundCellRenderer whiteBackgroundRenderer =
        new BackgroundCellRenderer(
            GuiHelper.getDefaultFont(), Colors.GRAY250, Colors.GRAY32, SwingConstants.LEFT);
    viewName.setCellRenderer(whiteBackgroundRenderer);
    creationDate.setCellRenderer(whiteBackgroundRenderer);
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
