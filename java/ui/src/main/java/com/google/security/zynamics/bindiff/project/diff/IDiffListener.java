package com.google.security.zynamics.bindiff.project.diff;

public interface IDiffListener {
  void closedView(final Diff diff);

  void loadedDiff(final Diff diff);

  void loadedView(final Diff diff);

  void removedDiff(final Diff diff);

  void unloadedDiff(final Diff diff);

  void willOverwriteDiff(String overridePath);
}
