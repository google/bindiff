// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.plugins;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Loads class bytes from a file.
 */
public class FileClassLoader extends MultiClassLoader {

  private final String filePrefix;

  /**
   * Attempts to load from a local file using the relative "filePrefix", ie starting at the current
   * directory. For example
   * 
   * @param filePrefix could be "webSiteClasses\\site1\\".
   */
  public FileClassLoader(final String filePrefix) {
    this.filePrefix = filePrefix;
  }

  @Override
  protected byte[] loadClassBytes(String className) {

    className = formatClassName(className);
    if (sourceMonitorOn) {
      print(">> from file: " + className);
    }
    byte result[];
    final String fileName = filePrefix + className;
    try {
      final FileInputStream inStream = new FileInputStream(fileName);
      result = new byte[inStream.available()];
      inStream.read(result);
      inStream.close();
      return result;
    } catch (final IOException e) {
      System.out.println("### File '" + fileName + "' not found.");
      return null;
    }
  }
}
