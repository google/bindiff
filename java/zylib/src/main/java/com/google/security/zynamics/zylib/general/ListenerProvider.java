// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.general;

import com.google.common.base.Preconditions;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

// TODO(jannewger): check if we can get rid of the LinkedHashSet: why would iteration order matter?
// Also: couldn't we simply use an ArrayList as internal storage? Should be way cheaper memory wise.
/**
 * Helper class that can be used by all classes that need to keep track of a list of listeners.
 *
 * <p>This class already takes care of parameter checking and proper iteration over the listener
 * list.
 *
 * <p>To use this class, simply add a field of type ListenerProvider<T> to your class and forward
 * the addListener and removeListener functions to the ListenerProvider functions. To notify the
 * listeners use a for-each loop on the ListenerProvider object.
 *
 * @param <T> The type of the listeners stored in the ListenerProvider.
 */
public class ListenerProvider<T> implements Iterable<T> {
  // Note: since the ListenerProvider is so frequently used, we initialize the m_listeners lazily.
  private volatile Collection<WeakReference<T>> m_listeners;

  /**
   * Adds a listener to the listener provider.
   *
   * @param listener The listener to add.
   * @throws NullPointerException Thrown if the listener object is null.
   * @throws IllegalStateException Thrown if the listener is already managed by the listener
   *     provider.
   */
  public void addListener(final T listener) {
    Preconditions.checkNotNull(listener, "Internal Error: Listener cannot be null");

    if (m_listeners == null) {
      synchronized (this) {
        if (m_listeners == null) {
          m_listeners = new LinkedHashSet<WeakReference<T>>();
        }
      }
    }

    synchronized (m_listeners) {
      if (!m_listeners.add(new ComparableReference(listener))) {
        // throw new IllegalStateException(String.format(
        // "Internal Error: Listener '%s' can not be added more than once.", listener));
      }
    }
  }

  @Override
  public Iterator<T> iterator() {
    if (m_listeners == null) {
      return Collections.emptyIterator();
    }

    final ArrayList<WeakReference<T>> listenersCopy;

    synchronized (m_listeners) {
      listenersCopy = new ArrayList<WeakReference<T>>(m_listeners);
    }

    final ArrayList<T> listeners = new ArrayList<T>();

    for (final WeakReference<T> weakT : listenersCopy) {
      final T element = weakT.get();

      if (element != null) {
        listeners.add(element);
      }
    }

    return listeners.iterator();
  }

  /**
   * Removes a listener from the listener provider.
   *
   * @param listener The listener to remove.
   * @throws NullPointerException Thrown if the listener object is null.
   * @throws IllegalStateException Thrown if the listener object was not managed by the listener
   *     provider.
   */
  public void removeListener(final T listener) {
    Preconditions.checkNotNull(listener, "Internal Error: Listener cannot be null");

    if (m_listeners != null) {
      // TODO(timkornau): b/15378569
      synchronized (m_listeners) {
        if (!m_listeners.remove(new ComparableReference(listener))) {
          // TODO (timkornau): this always happens if a script has been called and then after that
          // you try to close the view. throw new
          // IllegalStateException("Error: Listener was not listening.");
        }
      }
    }
  }

  /**
   * We need this special weak reference class because we store the listeners in a set and we need
   * set-comparability.
   */
  public class ComparableReference extends WeakReference<T> {
    public ComparableReference(final T referent) {
      super(referent);
    }

    @Override
    public boolean equals(final Object rhs) {
      if (rhs == null) {
        return false;
      }

      @SuppressWarnings("unchecked")
      final ComparableReference rhso = (ComparableReference) rhs;

      final T lhsElement = get();
      final T rhsElement = rhso.get();

      if ((lhsElement == null) && (rhsElement == null)) {
        return true;
      }
      if ((lhsElement == null) || (rhsElement == null)) {
        return false;
      }
      return lhsElement.equals(rhsElement);
    }

    @Override
    public int hashCode() {
      final T element = get();

      return element == null ? 0 : element.hashCode();
    }
  }
}
