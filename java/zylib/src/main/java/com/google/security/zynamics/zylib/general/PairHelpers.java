// Copyright 2011 Google Inc. All Rights Reserved.

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
