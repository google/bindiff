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
