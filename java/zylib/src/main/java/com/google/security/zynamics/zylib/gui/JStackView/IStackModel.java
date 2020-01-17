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

package com.google.security.zynamics.zylib.gui.JStackView;

/**
 * Must be implemented by all classes that want to supply data to stack views.
 */
public interface IStackModel {
  /**
   * Adds a listener that is notified about changes in the stack model.
   * 
   * @param listener The listener to add.
   */
  void addListener(IStackModelListener listener);

  /**
   * Returns the stack element at the given address.
   * 
   * @param address The address of the stack element.
   * 
   * @return The stack element at the given address.
   */
  String getElement(long address);

  /**
   * Returns the number of elements on the stack.
   * 
   * @return The number of elements on the stack.
   */
  int getNumberOfEntries();

  /**
   * Returns the current value of the stack pointer.
   * 
   * @return The current value of the stack pointer.
   */
  long getStackPointer();

  /**
   * Returns the start address of the stack.
   * 
   * @return The start address of the stack.
   */
  long getStartAddress();

  /**
   * Determines whether the given stack data is available.
   * 
   * @param startAddress Start address of the memory section.
   * @param numberOfBytes Number of bytes in the memory section.
   * 
   * @return True, if the data is available. False, otherwise.
   */
  boolean hasData(long startAddress, long numberOfBytes);

  /**
   * Returns whether the stack view should continue trying to display a given memory address.
   * 
   * @return True, to keep trying. False, otherwise.
   */
  boolean keepTrying();
}
