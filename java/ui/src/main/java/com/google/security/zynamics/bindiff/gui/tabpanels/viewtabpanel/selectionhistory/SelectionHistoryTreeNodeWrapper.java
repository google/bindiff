package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory;

public class SelectionHistoryTreeNodeWrapper {
  private final SelectionSnapshot operation;

  private final int count;

  public SelectionHistoryTreeNodeWrapper(final SelectionSnapshot snapshot, final int count) {
    operation = snapshot;
    this.count = count;
  }

  @Override
  public String toString() {
    return String.format(
        "%d-%s (%d)", count, operation.getDescription(), operation.getSelection().size());
  }
}
