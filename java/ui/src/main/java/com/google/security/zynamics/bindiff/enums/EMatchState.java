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

package com.google.security.zynamics.bindiff.enums;

public enum EMatchState {
  MATCHED,
  PRIMARY_UNMATCHED,
  SECONDRAY_UNMATCHED;

  public static EMatchState getMatchState(final int ordinal) {
    switch (ordinal) {
      case 0:
        return EMatchState.MATCHED;
      case 1:
        return EMatchState.PRIMARY_UNMATCHED;
      case 2:
        return EMatchState.SECONDRAY_UNMATCHED;
    }

    throw new IllegalArgumentException("Unknown match state.");
  }

  public static int getOrdinal(final EMatchState matchState) {
    switch (matchState) {
      case SECONDRAY_UNMATCHED:
        return 2; // 1
      case PRIMARY_UNMATCHED:
        return 1; // 2
      case MATCHED:
        return 0;
    }

    throw new IllegalArgumentException("Unknown match state type.");
  }

  @Override
  public String toString() {
    switch (this) {
      case MATCHED:
        return "Matched";
      case PRIMARY_UNMATCHED:
        return "Primary Unmatched";
      case SECONDRAY_UNMATCHED:
        return "Secondary Unmatched";
    }

    throw new IllegalArgumentException("Unknown match state type.");
  }
}
