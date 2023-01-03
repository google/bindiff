// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.gui.zygraph.edges;

public enum EdgeType {
  // conditional jumps
  JUMP_CONDITIONAL_TRUE,
  JUMP_CONDITIONAL_FALSE,
  JUMP_UNCONDITIONAL,
  JUMP_SWITCH,
  // loops
  JUMP_CONDITIONAL_TRUE_LOOP,
  JUMP_CONDITIONAL_FALSE_LOOP,
  JUMP_UNCONDITIONAL_LOOP,
  // inline
  ENTER_INLINED_FUNCTION,
  LEAVE_INLINED_FUNCTION,
  INTER_MODULE,
  INTER_ADDRESSSPACE_EDGE,
  // misc
  TEXTNODE_EDGE,
  DUMMY;
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
