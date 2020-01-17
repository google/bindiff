// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.enums;

public enum ESortByCriterion {
  NONE,
  ADDRESS,
  FUNCTION_TYPE,
  SIDE,
  MATCH_STATE,
  SELECTION,
  VISIBILITY,
  FUNCTION_NAME;

  private static final String SORT_CRITERION_NONE = "None";
  private static final String SORT_CRITERION_ADDRESS = "Address";
  private static final String SORT_CRITERION_FUNCTION_TYPE = "Function Type";
  private static final String SORT_CRITERION_SIDE = "Side";
  private static final String SORT_CRITERION_MATCH_STATE = "Match State";
  private static final String SORT_CRITERION_SELECTION = "Selection";
  private static final String SORT_CRITERION_VISIBILITY = "Visibility";
  private static final String SORT_CRITERION_FUNCTION_NAME = "Function Name";

  public static ESortByCriterion toSortCriterion(final String sortCriterion) {
    if (SORT_CRITERION_ADDRESS.equals(sortCriterion)) {
      return ADDRESS;
    }
    if (SORT_CRITERION_FUNCTION_TYPE.equals(sortCriterion)) {
      return FUNCTION_TYPE;
    }
    if (SORT_CRITERION_SIDE.equals(sortCriterion)) {
      return SIDE;
    }
    if (SORT_CRITERION_MATCH_STATE.equals(sortCriterion)) {
      return MATCH_STATE;
    }
    if (SORT_CRITERION_SELECTION.equals(sortCriterion)) {
      return SELECTION;
    }
    if (SORT_CRITERION_VISIBILITY.equals(sortCriterion)) {
      return VISIBILITY;
    }
    if (SORT_CRITERION_FUNCTION_NAME.equals(sortCriterion)) {
      return FUNCTION_NAME;
    }
    return NONE;
  }

  @Override
  public String toString() {
    switch (this) {
      case NONE:
        return SORT_CRITERION_NONE;
      case ADDRESS:
        return SORT_CRITERION_ADDRESS;
      case FUNCTION_TYPE:
        return SORT_CRITERION_FUNCTION_TYPE;
      case SIDE:
        return SORT_CRITERION_SIDE;
      case MATCH_STATE:
        return SORT_CRITERION_MATCH_STATE;
      case SELECTION:
        return SORT_CRITERION_SELECTION;
      case VISIBILITY:
        return SORT_CRITERION_VISIBILITY;
      case FUNCTION_NAME:
        return SORT_CRITERION_FUNCTION_NAME;
    }
    return SORT_CRITERION_NONE;
  }
}
