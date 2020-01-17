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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class WorkspaceTreePanel extends JPanel {
  private final WorkspaceTree workspaceTree;

  public WorkspaceTreePanel(final WorkspaceTabPanelFunctions controller) {
    super(new BorderLayout());

    Preconditions.checkNotNull(controller);

    workspaceTree = new WorkspaceTree(controller);

    final JScrollPane scrollPane = new JScrollPane(workspaceTree);
    scrollPane.setBorder(null);

    add(scrollPane, BorderLayout.CENTER);
  }

  public WorkspaceTree getWorkspaceTree() {
    return workspaceTree;
  }
}
