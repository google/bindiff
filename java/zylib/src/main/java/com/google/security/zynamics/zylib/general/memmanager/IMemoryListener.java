// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.general.memmanager;

/**
 * Interface that must be implemented by all classes that want to be notified about changes in the
 * simulated memory.
 */
public interface IMemoryListener {
  /**
   * Invoked after the content of the memory changed.
   * 
   * @param address The address of the first memory cell that changed.
   * @param size Number of consecutive bytes that changed.
   */
  void memoryChanged(long address, int size);

  /**
   * Invoked after the content of the memory was cleared.
   */
  void memoryCleared();
}
