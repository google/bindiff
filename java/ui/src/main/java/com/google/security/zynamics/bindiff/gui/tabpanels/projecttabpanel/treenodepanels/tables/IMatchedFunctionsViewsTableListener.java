package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables;

public interface IMatchedFunctionsViewsTableListener {
  void rowSelectionChanged(MatchedFunctionViewsTable table);

  void tableDataChanged(MatchedFunctionsViewsTableModel model);
}
