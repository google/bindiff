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

package com.google.security.zynamics.zylib.disassembly;

public enum FunctionType {
  // Please note that the order of the items is important because
  // the ordinal value of the enum members is used to sort the
  // functions in the function table.

  NORMAL,
  LIBRARY,
  IMPORT,
  THUNK,
  ADJUSTOR_THUNK,
  INVALID,
  UNKNOWN;

  public static FunctionType parseInt(final int value) {
    switch (value) {
      case 1:
        return NORMAL;
      case 2:
        return LIBRARY;
      case 3:
        return IMPORT;
      case 4:
        return THUNK;
      case 5:
        return ADJUSTOR_THUNK;
      case 6:
        return INVALID;
      case 7:
        return UNKNOWN;
      default:
        throw new IllegalArgumentException("Internal Error: Invalid function type " + value);
    }
  }

  @Override
  public String toString() {
    switch (this) {
      case NORMAL:
        return "Normal";
      case LIBRARY:
        return "Library";
      case IMPORT:
        return "Imported";
      case THUNK:
        return "Thunk";
      case ADJUSTOR_THUNK:
        return "Adjustor Thunk";
      case INVALID:
        return "Invalid";
      case UNKNOWN:
        return "Unknown";
      default:
        throw new IllegalArgumentException("Internal Error: Invalid function type");
    }
  }
}
