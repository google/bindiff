package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

public interface IUnmatchedFunctionsViewsTableListener {
  void rowSelectionChanged(UnmatchedFunctionViewsTable table);

  void tableDataChanged(UnmatchedFunctionViewsTableModel model);
}
