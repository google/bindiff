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

package com.google.security.zynamics.bindiff.utils;

public class FlagUtils {
  public static boolean parseFlag(final byte flags, final byte position) {
    if (position < 0 || position > 7) {
      throw new IllegalArgumentException("Flag has only 8 bits.");
    }

    final int shifted = flags >>> position;
    return 0 != (shifted & 0x01);
  }

  public static boolean parseFlag(final int flags, final byte position) {
    if (position < 0 || position > 31) {
      throw new IllegalArgumentException("Flag has only 32 bits.");
    }

    final int shifted = flags >>> position;
    return 0 != (shifted & 0x01);
  }

  public static byte setFlag(final byte flags, final byte position) {
    if (position < 0 || position > 7) {
      throw new IllegalArgumentException("Flag has only 8 bits.");
    }

    byte f = flags;
    f |= 1 << position;

    return f;
  }

  public static int setFlag(final int flags, final byte position) {
    if (position < 0 || position > 31) {
      throw new IllegalArgumentException("Flag has only 32 bits.");
    }

    int f = flags;
    f |= 1 << position;

    return f;
  }
}
