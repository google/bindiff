// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general;

@Deprecated
public class MathUtils {
  public static long pow(final long base, final long exponent) {
    if (exponent == 0) {
      return 1;
    } else {
      long start = base;

      for (int i = 1; i < exponent; i++) {
        start *= base;
      }

      return start;
    }
  }
}
