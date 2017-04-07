package com.google.security.zynamics.bindiff.project;

import com.google.security.zynamics.bindiff.project.diff.Diff;

/**
 * Simple adapter class for the workspace event listener. This avoids having empty implementations
 * scattered all over the code base.
 *
 * @author cblichmann@google.com (Christian Blichmann)
 */
public class WorkspaceAdapter implements IWorkspaceListener {
  @Override
  public void addedDiff(final Diff diff) {
    // Do nothing by default
  }

  @Override
  public void closedWorkspace() {
    // Do nothing by default
  }

  @Override
  public void loadedWorkspace(final Workspace workspace) {
    // Do nothing by default
  }

  @Override
  public void removedDiff(final Diff diff) {
    // Do nothing by default
  }
}
