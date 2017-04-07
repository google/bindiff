// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

/**
 * Enumeration type that describes the possible types of operand expressions.
 */
public enum ExpressionType {
  SYMBOL, IMMEDIATE_INTEGER, IMMEDIATE_FLOAT, OPERATOR, REGISTER, SIZE_PREFIX, MEMDEREF, EXPRESSION_LIST
}
