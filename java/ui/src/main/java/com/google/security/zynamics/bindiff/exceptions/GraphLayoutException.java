package com.google.security.zynamics.bindiff.exceptions;

public class GraphLayoutException extends Exception {
  public GraphLayoutException(final Exception e, final String msg) {
    super(msg, e);
  }

  public GraphLayoutException(final String msg) {
    super(msg);
  }
}
