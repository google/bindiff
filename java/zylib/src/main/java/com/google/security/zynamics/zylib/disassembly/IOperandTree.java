// Copyright 2011-2022 Google LLC
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
