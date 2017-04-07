// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui;

public abstract class DefaultWrapper<T> {
  private final T m_object;

  protected DefaultWrapper(final T object) {
    m_object = object;
  }

  public T getObject() {
    return m_object;
  }

  @Override
  public abstract String toString();
}
