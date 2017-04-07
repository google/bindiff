// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

public interface IReference {
  IAddress getTarget();

  ReferenceType getType();
}
