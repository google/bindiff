// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui;

import javax.swing.SwingUtilities;

public abstract class SwingInvoker {
  protected abstract void operation();

  public void invokeAndWait() {
    if (!SwingUtilities.isEventDispatchThread()) {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            operation();
          }
        });
      } catch (final Exception e) {
      }
    } else {
      operation();
    }
  }

  public void invokeLater() {
    if (!SwingUtilities.isEventDispatchThread()) {
      try {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            operation();
          }
        });
      } catch (final Exception e) {
      }
    } else {
      operation();
    }
  }
}
