package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import com.google.common.base.Preconditions;

import javax.swing.JCheckBox;

public class DiffPairTableData {
  private final JCheckBox selectionCheckbox = new JCheckBox();

  private final String idbName;
  private final String idbLocation;
  private String destinationDirectory;

  public DiffPairTableData(
      final String idbName, final String location, final String destinationDirectory) {
    this.idbName = Preconditions.checkNotNull(idbName);
    this.idbLocation = Preconditions.checkNotNull(location);
    this.destinationDirectory = Preconditions.checkNotNull(destinationDirectory);

    selectionCheckbox.setSelected(true);
  }

  public String getDestinationDirectory() {
    return destinationDirectory;
  }

  public String getIDBLocation() {
    return idbLocation;
  }

  public String getIDBName() {
    return idbName;
  }

  public JCheckBox getSelectionCheckBox() {
    return selectionCheckbox;
  }

  public void setDestinationDirectory(final String destinationDir) {
    destinationDirectory = destinationDir;
  }
}
