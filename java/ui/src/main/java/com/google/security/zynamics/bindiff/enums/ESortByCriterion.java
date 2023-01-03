// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.enums;

public enum ESortByCriterion {
  NONE("None"),
  ADDRESS("Address"),
  FUNCTION_TYPE("Function Type"),
  SIDE("Side"),
  MATCH_STATE("Match State"),
  SELECTION("Selection"),
  VISIBILITY("Visibility"),
  FUNCTION_NAME("Function Name");

  private final String sortCriterion;

  ESortByCriterion(final String sortCriterion) {
    this.sortCriterion = sortCriterion;
  }

  public static ESortByCriterion toSortCriterion(final String sortCriterion) {
    if (ADDRESS.toString().equals(sortCriterion)) {
      return ADDRESS;
    }
    if (FUNCTION_TYPE.toString().equals(sortCriterion)) {
      return FUNCTION_TYPE;
    }
    if (SIDE.toString().equals(sortCriterion)) {
      return SIDE;
    }
    if (MATCH_STATE.toString().equals(sortCriterion)) {
      return MATCH_STATE;
    }
    if (SELECTION.toString().equals(sortCriterion)) {
      return SELECTION;
    }
    if (VISIBILITY.toString().equals(sortCriterion)) {
      return VISIBILITY;
    }
    if (FUNCTION_NAME.toString().equals(sortCriterion)) {
      return FUNCTION_NAME;
    }
    return NONE;
  }

  @Override
  public String toString() {
    return sortCriterion;
  }
}
