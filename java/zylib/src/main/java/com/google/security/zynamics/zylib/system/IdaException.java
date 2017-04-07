// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.system;

/**
 * Exception class used to signal problems loading IDA Pro.
 * 
 * @author thomasdullien@google.com (Thomas Dullien)
 */
public final class IdaException extends Exception {
  /**
   * Creates a new exception object.
   * 
   * @param msg The exception message.
   * @param cause The original cause
   */
  public IdaException(final String msg, final Throwable cause) {
    super(msg, cause);
  }
}
