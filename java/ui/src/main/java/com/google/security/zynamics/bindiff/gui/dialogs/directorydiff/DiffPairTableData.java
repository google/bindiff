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
