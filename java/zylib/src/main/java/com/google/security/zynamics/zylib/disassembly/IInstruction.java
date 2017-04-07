// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

import java.util.List;

public interface IInstruction {

  IAddress getAddress();

  String getArchitecture();

  byte[] getData();

  long getLength();

  String getMnemonic();

  // TODO(jannewger): this should be pulled up into ReilInstruction since there are no additional
  // cross references to this method.
  Integer getMnemonicCode();

  List<? extends IOperandTree> getOperands();
}
