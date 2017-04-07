// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general;

import com.google.common.base.Preconditions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * This class provides quick-access functions to the system clipboard. These functions can be used
 * to quickly store data to or retrieve data from the system clipboard.
 */
public final class ClipboardHelpers {
  /**
   * Copies a string to the system clipboard.
   *
   * @param string The string to be copied to the system clipboard.
   */
  public static void copyToClipboard(final String string) {
    Preconditions.checkNotNull(string, "Error: String argument can not be null");

    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    clipboard.setContents(new StringSelection(string), new ClipboardOwner() {
      @Override
      public void lostOwnership(final Clipboard clipboard, final Transferable contents) {}
    });
  }

  /**
   * Returns the string that is currently stored in the system clipboard.
   *
   * @return The string from the system clipboard or null if there is no string currently stored in
   *         the clipboard.
   */
  public static String getClipboardString() {

    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    final Transferable contents = clipboard.getContents(null);

    final boolean hasTransferableText =
        (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
    if (!hasTransferableText) {
      return null;
    }

    try {
      return (String) contents.getTransferData(DataFlavor.stringFlavor);
    } catch (final UnsupportedFlavorException | IOException ex) {
      // Eat, cannot happen as we're checking above
    }

    return null;
  }
}
