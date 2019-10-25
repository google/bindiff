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
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.tables.CTableSorter;
import java.awt.Font;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ExtendedMatchedFunctionViewsTable extends AbstractTable {
  public ExtendedMatchedFunctionViewsTable(
      final AbstractTableModel model, final WorkspaceTabPanelFunctions controller) {
    super(model, controller);

    sortColumn(ExtendedMatchedFunctionViewsTableModel.SIMILARITY, CTableSorter.DESCENDING);
    sortColumn(ExtendedMatchedFunctionViewsTableModel.PRIMARY_TYPE, CTableSorter.ASCENDING);
    sortColumn(ExtendedMatchedFunctionViewsTableModel.CONFIDENCE, CTableSorter.ASCENDING);
    sortColumn(ExtendedMatchedFunctionViewsTableModel.PRIMARY_ADDRESS, CTableSorter.ASCENDING);

    init();
  }

  private void init() {
    final TableColumnModel model = getColumnModel();
    final TableColumn icon = model.getColumn(ExtendedMatchedFunctionViewsTableModel.ICON);
    final TableColumn primaryAddr =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.PRIMARY_ADDRESS);
    final TableColumn primaryName =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.PRIMARY_NAME);
    final TableColumn primaryType =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.PRIMARY_TYPE);
    final TableColumn basicBlockMatches =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.BASICBLOCK_MATCHES);
    final TableColumn similarity =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.SIMILARITY);
    final TableColumn confidence =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.CONFIDENCE);
    final TableColumn jumpMatches =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.JUMP_MATCHES);
    final TableColumn secondaryType =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.SECONDARY_TYPE);
    final TableColumn secondaryName =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.SECONDARY_NAME);
    final TableColumn secondaryAddr =
        model.getColumn(ExtendedMatchedFunctionViewsTableModel.SECONDARY_ADDRESS);

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
    final AbstractTableModel model = getTableModel();
    final IAddress primaryAddr =
        new CAddress(
            (String) model.getValueAt(row, ExtendedMatchedFunctionViewsTableModel.PRIMARY_ADDRESS),
            16);
    final IAddress secondaryAddr =
        new CAddress(
            (String)
                model.getValueAt(row, ExtendedMatchedFunctionViewsTableModel.SECONDARY_ADDRESS),
            16);

    final WorkspaceTabPanelFunctions controller = getController();
    controller.openFlowGraphView(controller.getMainWindow(), getDiff(), primaryAddr, secondaryAddr);
  }

  @Override
  public void dispose() {}
}
