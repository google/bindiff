// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.scripting;

public class InitStringFactory {
  public static String getInitString(final String string, final String consoleName,
      final String libPath) {
    if (string == null) {
      return "";
    }

    if (string.startsWith("python")) {
      return String.format("import sys\nsys.stdout = %s\nsys.path.append('%s')\n", consoleName,
          libPath);
    }

    return "";
  }
}
