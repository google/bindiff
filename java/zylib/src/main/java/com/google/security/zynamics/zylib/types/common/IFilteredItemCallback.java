// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.common;

public interface IFilteredItemCallback<ItemType> extends IItemCallback<ItemType>,
    ICollectionFilter<ItemType> {

}
