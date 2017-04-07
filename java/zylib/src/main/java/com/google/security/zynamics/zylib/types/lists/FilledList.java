// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.lists;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ArrayList which is guaranteed not to have null-elements.
 *
 * @param <T> Type of the objects in the list.
 */
public class FilledList<T> extends ArrayList<T> implements IFilledList<T> {

  /**
   * Creates an empty filled list.
   */
  public FilledList() {
  }

  /**
   * Creates a filled list from a collection.
   *
   * @param collection The collection whose elements are put into the FilledList.
   */
  public FilledList(final Collection<? extends T> collection) {
    super(collection);

    for (final T t : collection) {
      Preconditions.checkNotNull(t, "Error: Can not add null-elements to filled lists");
    }
  }

  @Override
  public void add(final int index, final T o) {
    Preconditions.checkNotNull(o, "Error: Can not add null-elements to filled lists");

    super.add(index, o);
  }

  @Override
  public boolean add(final T o) {
    Preconditions.checkNotNull(o, "Error: Can not add null-elements to filled lists");

    return super.add(o);
  }

  @Override
  public boolean addAll(final Collection<? extends T> c) {
    for (final T t : c) {
      Preconditions.checkNotNull(t, "Error: Can not add null-elements to filled lists");
    }

    return super.addAll(c);
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends T> c) {
    for (final T t : c) {
      Preconditions.checkNotNull(t, "Error: Can not add null-elements to filled lists");
    }

    return super.addAll(index, c);
  }
}
