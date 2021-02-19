// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.types.common;

public interface IItemCallback<ItemType> {
  /**
   * This function is called by the iterator for each item of a collection.
   * 
   * @param item An item in the collection.
   * 
   * @return Information that's passed back to the iterator object to help the object to find out
   *         what to do next.
   */
  IterationMode next(ItemType item);
}
