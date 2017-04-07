// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.strings;

/**
 * This class can be used to create comma-separated lists of objects.
 */
public final class Commafier {
  private static String DEFAULT_SEPARATOR = ", ";

  private final String m_separator;

  private final StringBuilder sb = new StringBuilder();

  private boolean needsComma = false;

  /**
   * Creates a Commafier object that starts with an empty string.
   */
  public Commafier() {
    m_separator = DEFAULT_SEPARATOR;
  }

  /**
   * Creates a Commafier object that starts with a commafied list that contains the given elements.
   * 
   * @param elements The elements that are commafied into the list.
   */
  public Commafier(final Iterable<? extends Object> elements) {
    this(elements, DEFAULT_SEPARATOR);
  }

  /**
   * Creates a Commafier object that starts with a commafied list that contains the given elements.
   * 
   * @param elements The elements that are commafied into the list.
   */
  public Commafier(final Iterable<? extends Object> elements, final String separator) {
    m_separator = separator;

    for (final Object element : elements) {
      append(element);
    }
  }

  /**
   * Creates a Commafier object that starts with an initial string.
   * 
   * @param initial The initial string.
   */
  public Commafier(final String initial) {
    m_separator = DEFAULT_SEPARATOR;

    sb.append(initial);
  }

  /**
   * Commafies a list of elements and returns the commafied string.
   * 
   * @param elements The elements to commafy.
   * 
   * @return The commafied string.
   */
  public static String commafy(final Iterable<? extends Object> elements) {
    return new Commafier(elements).toString();
  }

  public static String commafy(final Iterable<? extends Object> elements, final String separator) {
    return new Commafier(elements, separator).toString();
  }

  /**
   * Appends a new value to the commafied list.
   * 
   * @param value The value to add to the commafied list.
   */
  public void append(final Object value) {
    if (needsComma) {
      sb.append(m_separator);
    }

    needsComma = true;

    sb.append(value);
  }

  /**
   * Appends a value to the commafied list without prepending a comma.
   * 
   * @param value The value to be appended.
   */
  public void appendUncommafied(final String value) {
    sb.append(value);
  }

  @Override
  public String toString() {
    return sb.toString();
  }
}
