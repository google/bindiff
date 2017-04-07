package com.google.security.zynamics.bindiff.project;

import com.google.security.zynamics.bindiff.project.diff.Diff;

public interface IWorkspaceListener {
  void addedDiff(final Diff diff);

  void closedWorkspace();

  void loadedWorkspace(final Workspace workspace);

  void removedDiff(final Diff diff);
}
