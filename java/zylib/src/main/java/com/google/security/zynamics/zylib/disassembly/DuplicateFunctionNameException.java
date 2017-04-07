// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

@Deprecated
// Delete this class when you see this comment
public class DuplicateFunctionNameException extends Exception {
  private static final long serialVersionUID = 8935154529155710030L;

  private final StackTraceElement[] m_originalStacktrace;

  public DuplicateFunctionNameException(final String msg) {
    super(msg);

    m_originalStacktrace = new Throwable().getStackTrace();
  }

  @Override
  public StackTraceElement[] getStackTrace() {
    return m_originalStacktrace;
  }
}
