// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.JRegisterView;

import java.math.BigInteger;

public class RegisterInformationInternal extends RegisterInformation {
  private BigInteger value = BigInteger.ZERO;
  private boolean modified = false;

  public RegisterInformationInternal(final String registerName, final int registerSize) {
    super(registerName, registerSize);
  }

  public BigInteger getValue() {
    return value;
  }

  public boolean isModified() {
    return modified;
  }

  public void setModified(final boolean modified) {
    this.modified = modified;
  }

  public void setValue(final BigInteger value) {
    this.value = value;
  }
}
