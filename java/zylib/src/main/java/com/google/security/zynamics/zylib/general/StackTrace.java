// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
