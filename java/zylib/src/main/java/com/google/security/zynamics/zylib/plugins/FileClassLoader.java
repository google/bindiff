// Copyright 2011-2022 Google LLC
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
