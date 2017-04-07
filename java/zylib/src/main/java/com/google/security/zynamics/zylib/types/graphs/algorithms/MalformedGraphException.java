// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.graphs.algorithms;

/**
 * Exception that is used to signal that there is something wrong with the graph.
 */
public class MalformedGraphException extends Exception {
  private static final long serialVersionUID = 7422498674681635996L;

  /**
   * Creates a new MalformedGraphException object.
   * 
   * @param msg The exception message.
   */
  public MalformedGraphException(final String msg) {
    super(msg);
  }
}
