// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.gui.ProgressDialogs;

import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.lang.reflect.InvocationTargetException;

/**
 * This class is a helper thread to use with the CEndlessProgressDialog class. To use this class do
 * the following.
 *
 * <p>1. Subclass this class and provide the implementation of runExpensiveCommand 2. Create an
 * instance (called for example thread) of the subclassed class. 3. Create the endless progress
 * dialog (dlg) and pass the instance to the dialog constructor 4. Execute thread.start() on the
 * instance from step #2 5. Execute dlg.setVisible(true)
 */
public abstract class CStandardHelperThread extends Thread
    implements IStandardProgressModel, IStandardDescriptionUpdater {
  private final ListenerProvider<IStandardProgressListener> m_listeners =
      new ListenerProvider<IStandardProgressListener>();

  private Exception m_exception;

  private void notifyListeners() {
    for (final IStandardProgressListener listener : m_listeners) {
      listener.finished();
    }
  }

  @SuppressWarnings("deprecation")
  protected void finish() {
    notifyListeners();

    threadStop(this);
  }

  private static void threadStop(Thread thread) {
    // TODO: b/447223240 - clean up obsolete references to Thread.stop.
    // Thread#stop has been deprecated since JDK 1.2. Starting in JDK 20 it always throws
    // UnsupportedOperationException, and in JDK 26 the method has been removed.
    try {
      Thread.class.getMethod("stop").invoke(thread);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof UnsupportedOperationException) {
        throw (UnsupportedOperationException) e.getCause();
      }
      throw new UnsupportedOperationException(e);
    } catch (ReflectiveOperationException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  protected abstract void runExpensiveCommand() throws Exception;

  @Override
  public final void addProgressListener(final IStandardProgressListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public void closeRequested() {
    // Do nothing
  }

  public Exception getException() {
    return m_exception;
  }

  @Override
  public void next() {
    for (final IStandardProgressListener listener : m_listeners) {
      listener.next();
    }
  }

  @Override
  public final void removeProgressListener(final IStandardProgressListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void reset() {
    for (final IStandardProgressListener listener : m_listeners) {
      listener.reset();
    }
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
    for (final IStandardProgressListener listener : m_listeners) {
      listener.changedDescription(description);
    }
  }

  @Override
  public void setMaximum(final int maximum) {
    for (final IStandardProgressListener listener : m_listeners) {
      listener.changedMaximum(maximum);
    }
  }
}
