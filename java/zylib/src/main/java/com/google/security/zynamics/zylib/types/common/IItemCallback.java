// Copyright 2011 Google Inc. All Rights Reserved.

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
