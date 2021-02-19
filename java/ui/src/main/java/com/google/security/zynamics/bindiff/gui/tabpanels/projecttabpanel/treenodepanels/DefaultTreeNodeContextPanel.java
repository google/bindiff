// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.resources.Constants;
import com.google.security.zynamics.bindiff.utils.ResourceUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JLabel;

public class DefaultTreeNodeContextPanel extends AbstractTreeNodeContextPanel {

  public DefaultTreeNodeContextPanel() {
    setMinimumSize(new Dimension(0, 0));
    setBackground(Color.WHITE);

    add(new JLabel(ResourceUtils.getImageIcon(Constants.DEFAULT_BACKGROUND_IMAGE_PATH, this)));
  }

  @Override
  public List<AbstractTable> getTables() {
    return null;
  }
}
