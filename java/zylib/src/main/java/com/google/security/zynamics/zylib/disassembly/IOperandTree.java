// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

public interface IOperandTree {
  public static final int NODE_TYPE_MNEMONIC_ID = 0;
  public static final int NODE_TYPE_SYMBOL_ID = 1;
  public static final int NODE_TYPE_IMMEDIATE_INT_ID = 2;
  public static final int NODE_TYPE_IMMEDIATE_FLOAT_ID = 3;
  public static final int NODE_TYPE_OPERATOR_ID = 4;
  public static final int NODE_TYPE_REGISTER_ID = 5;
  public static final int NODE_TYPE_SIZE_PREFIX_ID = 6;
  public static final int NODE_TYPE_DEREFERENCE_ID = 7;

  /**
   * Returns the root node of the operand tree.
   * 
   * @return The root node of the operand tree.
   */
  IOperandTreeNode getRootNode();

}
