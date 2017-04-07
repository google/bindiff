// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

public interface IModuleConfiguration {
  IAddress getFileBase();

  /**
   * Returns the name of the module.
   * 
   * @return The name of the module. This value is guaranteed to be non-null.
   */
  String getName();
}
