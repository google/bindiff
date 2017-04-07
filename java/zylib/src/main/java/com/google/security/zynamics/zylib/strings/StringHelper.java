// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.strings;

/**
 * Small helper class for String related helper functions.
 */
public class StringHelper {
  /**
   * Counts how often a character appears in a given string.
   *
   * @param string The string to search through.
   * @param c The character to search for.
   *
   * @return Number of times the character appears in the string.
   */
  public static int count(final String string, final char c) {
    int counter = 0;

    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) == c) {
        counter++;
      }
    }

    return counter;
  }

  /**
   * Replaces all occurrences of a substring inside a string.
   *
   * @param inputLine The input string.
   * @param source The substring to be replaced.
   * @param target The replacement of the substring.
   *
   * @return The input line with all occurrences of source replaced by target.
   */
  public static String replaceAll(final String inputLine, final String source, final String target) {
    int index = inputLine.indexOf(source);

    String ret = inputLine;

    while (index != -1) {
      ret = ret.substring(0, index) + target + ret.substring(index + source.length());
      index = ret.indexOf(source);
    }

    return ret;
  }
}
