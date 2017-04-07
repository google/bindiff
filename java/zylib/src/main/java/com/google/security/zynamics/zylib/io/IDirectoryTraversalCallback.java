// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.io;

import java.io.File;

/**
 * Interface to be implemented by objects that want to traverse through directories.
 */
public interface IDirectoryTraversalCallback {
  /**
   * Called when a new directory is entered.
   * 
   * @param directory The directory to be entered.
   */
  void entering(File directory);

  /**
   * Called when the current directory is left.
   * 
   * @param directory The current directory.
   */
  void leaving(File directory);

  /**
   * Called on each file that is found during the directory traversal.
   * 
   * @param file The next file that was found inside the current directory.
   */
  void nextFile(File file);
}
