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

package com.google.security.zynamics.bindiff.enums;

public enum ECircularLayoutStyle {
  COMPACT,
  ISOLATED,
  SINGLE_CYCLE;

  public static ECircularLayoutStyle getEnum(final int style) {
    switch (style) {
      case 0:
        return COMPACT;
      case 1:
        return ISOLATED;
      case 2:
        return SINGLE_CYCLE;
    }

    throw new IllegalArgumentException("Unknown circular layout style.");
  }

  public static int getOrdinal(final ECircularLayoutStyle style) {
    switch (style) {
      case COMPACT:
        return 0;
      case ISOLATED:
        return 1;
      case SINGLE_CYCLE:
        return 2;
    }

    throw new IllegalArgumentException("Unknown cicular layout style.");
  }
}
