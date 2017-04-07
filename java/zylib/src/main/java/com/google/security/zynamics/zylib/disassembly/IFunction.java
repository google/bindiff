// Copyright 2011 Google Inc. All Rights Reserved.

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
