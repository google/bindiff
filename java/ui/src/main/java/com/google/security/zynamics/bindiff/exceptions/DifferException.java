package com.google.security.zynamics.bindiff.exceptions;

public class DifferException extends Exception {
  public DifferException(final Exception e, final String msg) {
    super(msg, e);
  }

  public DifferException(final String msg) {
    super(msg);
  }
}
