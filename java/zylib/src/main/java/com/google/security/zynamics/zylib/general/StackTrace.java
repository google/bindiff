// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general;

import com.google.common.base.Preconditions;

/**
 * Simple helper class for stacktrace related helper methods.
 */
public class StackTrace {
  /**
   * Converts a stack trace to a string.
   * 
   * @param stackTrace The stack trace in question.
   * 
   * @return The converted stack trace.
   */
  public static String toString(final StackTraceElement[] stackTrace) {
    Preconditions.checkNotNull(stackTrace, "Invalid stack trace");

    final StringBuilder sb = new StringBuilder();

    for (final StackTraceElement stackTraceElement : stackTrace) {
      sb.append(stackTraceElement.toString());
      sb.append(System.getProperty("line.separator"));
    }

    return sb.toString();
  }
}
