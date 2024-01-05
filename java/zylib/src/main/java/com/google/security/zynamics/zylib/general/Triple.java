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

public class Triple<S, T, U> {
  private final S m_first;

  private final T m_second;

  private final U m_third;

  public Triple(final S first, final T second, final U third) {
    this.m_first = first;
    this.m_second = second;
    this.m_third = third;
  }

  public static <S, T, U> Triple<S, T, U> make(final S s, final T t, final U u) {
    return new Triple<S, T, U>(s, t, u);
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Triple<?, ?, ?>)) {
      return false;
    }

    final Triple<?, ?, ?> t = (Triple<?, ?, ?>) obj;

    if (!((t.first() == null) && (m_first == null))) {
      if ((t.first() == null) || (m_first == null)) {
        return false;
      } else if (!t.m_first.equals(m_first)) {
        return false;
      }
    }

    if (!((t.second() == null) && (m_second == null))) {
      if ((t.second() == null) || (m_second == null)) {
        return false;
      } else if (!t.m_second.equals(m_second)) {
        return false;
      }
    }

    if (!((t.third() == null) && (m_third == null))) {
      if ((t.third() == null) || (m_third == null)) {
        return false;
      } else if (!t.m_third.equals(m_third)) {
        return false;
      }
    }

    return true;
  }

  public S first() {
    return m_first;
  }

  @Override
  public int hashCode() {
    return (m_first == null ? 1 : m_first.hashCode())
        * (m_second == null ? 1 : m_second.hashCode())
        * (m_third == null ? 1 : m_third.hashCode());
  }

  public T second() {
    return m_second;
  }

  public U third() {
    return m_third;
  }

  @Override
  public String toString() {
    return "< " + m_first + ", " + m_second + ", " + m_third + ">";
  }
}
