package com.google.security.zynamics.bindiff.exceptions;

public class BinExportException extends Exception {
  public BinExportException(final Exception e) {
    super(e);
  }

  public BinExportException(final Exception e, final String msg) {
    super(msg, e);
  }

  public BinExportException(final String msg) {
    super(msg);
  }
}
