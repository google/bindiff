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
