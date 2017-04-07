package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

public interface ISelectionHistoryListener {
  void finishedRedo();

  void finishedUndo();

  void snapshotAdded(SelectionSnapshot command);

  void snapshotRemoved();

  void startedRedo();

  void startedUndo();
}
