// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.strings;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages a string which behaves like a circular buffer with a fixed number of lines.
 */
public class CircularStringBuffer {
  /**
   * FIFO string container.
   */
  private final Queue<String> m_buffer = new LinkedList<String>();

  /**
   * Maximum number of lines to be held in the buffer.
   */
  private final int m_maxSize;

  /**
   * Creates a new instance of the circular string buffer.
   * 
   * @param maxLines The maximum number of lines which are held by the string buffer.
   */
  public CircularStringBuffer(final int maxLines) {
    m_maxSize = maxLines;
  }

  private void addToBuffer(final String[] lines) {
    final int index = 0;

    for (int i = 0; i < lines.length; ++i) {
      while ((m_maxSize - m_buffer.size()) <= 0) {
        m_buffer.remove();
      }
      m_buffer.add(lines[index]);
    }
  }

  /**
   * Adds the given text to the circular buffer.
   * 
   * @param text The text to be added to the buffer.
   */
  public void add(final String text) {
    final String[] lines = text.split("\n");
    if (lines.length > 0) {
      addToBuffer(lines);
    } else {
      addToBuffer(new String[] {text});
    }
  }

  /**
   * Return the number of lines in the buffer.
   * 
   * @return The number of lines in the buffer.
   */
  public int getSize() {
    return m_buffer.size();
  }

  /**
   * Returns the circular buffer in one string object.
   * 
   * @return The whole buffer represented as one string.
   */
  public String getText() {
    final StringBuilder sb = new StringBuilder();
    for (final String s : m_buffer) {
      sb.append(s);
      sb.append('\n');
    }
    return sb.toString();
  }
}
