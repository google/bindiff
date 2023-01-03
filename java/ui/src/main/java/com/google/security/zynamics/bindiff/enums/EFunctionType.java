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

import com.google.security.zynamics.bindiff.resources.Colors;
import java.awt.Color;

public enum EFunctionType {
  NORMAL("Normal") {
    @Override
    public Color getColor() {
      return Color.BLACK;
    }
  },
  LIBRARY("Library") {
    @Override
    public Color getColor() {
      return Colors.FUNCTION_TYPE_LIBRARY.darker();
    }
  },
  THUNK("Thunk") {
    @Override
    public Color getColor() {
      return Colors.FUNCTION_TYPE_THUNK.darker();
    }
  },
  IMPORTED("Imported") {
    @Override
    public Color getColor() {
      return Colors.FUNCTION_TYPE_IMPORTED.darker();
    }
  },
  UNKNOWN("Unknown") {
    @Override
    public Color getColor() {
      return Colors.FUNCTION_TYPE_UNKNOWN;
    }
  },
  MIXED("Mixed Function Match") {
    @Override
    public Color getColor() {
      return Colors.MIXED_BASE_COLOR;
    }
  };

  private static final EFunctionType[] values = values();

  private final String displayName;

  EFunctionType(final String displayName) {
    this.displayName = displayName;
  }

  public static EFunctionType getType(int ordinal) {
    return values[ordinal];
  }

  public abstract Color getColor();

  @Override
  public String toString() {
    return displayName;
  }
}
