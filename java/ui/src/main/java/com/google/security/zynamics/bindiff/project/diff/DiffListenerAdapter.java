package com.google.security.zynamics.bindiff.project.diff;

/**
 * Simple adapter class for the Diff event listener. This avoids having empty implementations
 * scattered all over the code base.
 *
 * @author cblichmann@google.com (Christian Blichmann)
 */
public class DiffListenerAdapter implements IDiffListener {
  @Override
  public void closedView(final Diff diff) {
    // Do nothing by default
  }

  @Override
  public void loadedDiff(final Diff diff) {
    // Do nothing by default
  }

  @Override
  public void loadedView(final Diff diff) {
    // Do nothing by default
  }

  @Override
  public void removedDiff(final Diff diff) {
    // Do nothing by default
  }

  @Override
  public void unloadedDiff(final Diff diff) {
    // Do nothing by default
  }

  @Override
  public void willOverwriteDiff(final String overwritePath) {
    // Do nothing by default
  }
}
