// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.edges;

public enum EdgeType {
  // conditional jumps
  JUMP_CONDITIONAL_TRUE, JUMP_CONDITIONAL_FALSE, JUMP_UNCONDITIONAL, JUMP_SWITCH,
  // loops
  JUMP_CONDITIONAL_TRUE_LOOP, JUMP_CONDITIONAL_FALSE_LOOP, JUMP_UNCONDITIONAL_LOOP,
  // inline
  ENTER_INLINED_FUNCTION, LEAVE_INLINED_FUNCTION, INTER_MODULE, INTER_ADDRESSSPACE_EDGE,
  // misc
  TEXTNODE_EDGE, DUMMY;
  // INTER_FUNCTION,

  public static boolean isFalseEdge(final EdgeType type) {
    if ((type == JUMP_CONDITIONAL_FALSE) || (type == JUMP_CONDITIONAL_FALSE_LOOP)) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isTrueEdge(final EdgeType type) {
    return !isFalseEdge(type);
  }
}
