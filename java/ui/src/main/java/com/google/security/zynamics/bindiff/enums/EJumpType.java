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

public enum EJumpType {
  JUMP_TRUE,
  JUMP_FALSE,
  UNCONDITIONAL,
  SWITCH;

  public static final int getOrdinal(final EJumpType type) {
    switch (type) {
      case JUMP_TRUE:
        return 0;
      case JUMP_FALSE:
        return 1;
      case UNCONDITIONAL:
        return 2;
      case SWITCH:
        return 3;
      default:
        throw new IllegalArgumentException("Unknown jump type");
    }
  }

  public static EJumpType getType(final int ordinal) {
    switch (ordinal) {
      case 0:
        return JUMP_TRUE;
      case 1:
        return JUMP_FALSE;
      case 2:
        return UNCONDITIONAL;
      case 3:
        return SWITCH;
      default:
        throw new IllegalArgumentException("Unknown jump type");
    }
  }
}
