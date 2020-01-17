// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.disassembly;

/**
 * Interface for function methods.
 */
public interface IFunction {
  /**
   * Return the address of the function.
   *
   * @return The address of the function.
   */
  IAddress getAddress();

  /**
   * Get the basic block count of the function.
   *
   * @return The basic block count of the function.
   */
  int getBasicBlockCount();

  /**
   * Get the description of the function.
   *
   * @return The description of the function.
   */
  String getDescription();

  /**
   * Get the edge count of the function.
   *
   * @return The edge count of the function.
   */
  int getEdgeCount();

  /**
   * Get the indegree of the function.
   *
   * @return The indegree of the function.
   */
  int getIndegree();

  /**
   * Get the name of the function.
   *
   * @return The name of the function.
   */
  String getName();

  /**
   * Get the outdegree of the function.
   *
   * @return The outdegree of the function.
   */
  int getOutdegree();

  /**
   * Get the type of the function.
   *
   * @return The type of the function.
   */
  FunctionType getType();
}
