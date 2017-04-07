package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.subpanels;

public interface IViewsFilterCheckboxListener {
  void functionViewsFilterChanged(
      boolean structuralChange, boolean instructionOnlyChange, boolean matched);
}
