// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * This class is a helper thread to use with the CEndlessProgressDialog class. To use this class do
 * the following.
 * 
 * 1. Subclass this class and provide the implementation of runExpensiveCommand 2. Create an
 * instance (called for example thread) of the subclassed class. 3. Create the endless progress
 * dialog (dlg) and pass the instance to the dialog constructor 4. Execute thread.start() on the
 * instance from step #2 5. Execute dlg.setVisible(true)
 */
public abstract class CEndlessHelperThread extends Thread implements IEndlessProgressModel,
    IEndlessDescriptionUpdater {
  private final ListenerProvider<IEndlessProgressListener> m_listeners =
      new ListenerProvider<IEndlessProgressListener>();

  private Exception m_exception;

  private void notifyListeners() {
    for (final IEndlessProgressListener listener : m_listeners) {
      listener.finished();
    }
  }

  @SuppressWarnings("deprecation")
  protected void finish() {
    notifyListeners();

    // TODO: Check, who is notified? If it's NOT the thread itself, "interrupt" could possible be
    // called in
    // order to stop this thread in a non deprecated way (but only if isInterruped() is proved
    // within
    // the threads
    // run routine, and when true, return from thread function)
    stop();
  }

  protected abstract void runExpensiveCommand() throws Exception;

  @Override
  public final void addProgressListener(final IEndlessProgressListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public void closeRequested() {
  }

  public Exception getException() {
    return m_exception;
  }

  @Override
  public final void removeProgressListener(final IEndlessProgressListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public final void run() {
    try {
      runExpensiveCommand();
    } catch (final Exception exception) {
      m_exception = exception;
    } finally {
      notifyListeners();
    }
  }

  @Override
  public void setDescription(final String description) {
    for (final IEndlessProgressListener listener : m_listeners) {
      listener.changedDescription(description);
    }
  }

  public void setGeneralDescription(final String description) {
    for (final IEndlessProgressListener listener : m_listeners) {
      listener.changedGeneralDescription(description);
    }
  }
}
