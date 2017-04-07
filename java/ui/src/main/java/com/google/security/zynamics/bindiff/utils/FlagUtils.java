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
