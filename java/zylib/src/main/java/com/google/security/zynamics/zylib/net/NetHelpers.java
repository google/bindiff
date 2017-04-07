// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.net;

public class NetHelpers {
  private static final int MAX_PORT = 65535;

  public static boolean isValidPort(final int port) {
    return (port >= 0) && (port <= MAX_PORT);
  }

  public static boolean isValidPort(final String port) {
    try {
      Integer.parseInt(port);
      return true;
    } catch (final NumberFormatException e) {
      return false;
    }
  }
}
