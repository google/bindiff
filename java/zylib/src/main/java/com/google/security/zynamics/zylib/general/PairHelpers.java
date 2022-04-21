// Copyright 2011-2022 Google LLC
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

import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods that are useful when working with Pair objects.
 */
public final class PairHelpers {
  /**
   * Takes a list of <S, T> pairs and retrieves all S elements from the pairs in the list.
   * 
   * @param <S> Type of the first element of the pairs in the list.
   * @param <T> Type of the second element of the pairs in the list.
   * 
   * @param list The list of <S, T> pairs.
   * 
   * @return A list of all S elements of the input list.
   */
  public static <S, T> List<S> projectFirst(final List<Pair<S, T>> list) {
    final List<S> outputList = new ArrayList<S>();

    for (final Pair<S, T> pair : list) {
      outputList.add(pair.first());
    }

    return outputList;
  }

  /**
   * Takes a list of <S, T> pairs and retrieves all T elements from the pairs in the list.
   * 
   * @param <S> Type of the first element of the pairs in the list.
   * @param <T> Type of the second element of the pairs in the list.
   * 
   * @param list The list of <S, T> pairs.
   * 
   * @return A list of all T elements of the input list.
   */
  public static <S, T> List<T> projectSecond(final List<Pair<S, T>> list) {
    final List<T> outputList = new ArrayList<T>();

    for (final Pair<?, T> pair : list) {
      outputList.add(pair.second());
    }

    return outputList;
  }
}
