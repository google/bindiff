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
