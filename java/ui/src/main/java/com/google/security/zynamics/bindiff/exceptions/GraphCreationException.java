package com.google.security.zynamics.bindiff.exceptions;

public class GraphCreationException extends Exception {
  public GraphCreationException(final Exception e, final String msg) {
    super(msg, e);
  }

  public GraphCreationException(final String msg) {
    super(msg);
  }
}
