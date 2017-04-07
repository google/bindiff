// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.lists;

import java.util.List;

/**
 * List which is guaranteed not to have null-elements.
 * 
 * @param <T> Type of the elements in the list.
 */
public interface IFilledList<T> extends List<T> {
}
