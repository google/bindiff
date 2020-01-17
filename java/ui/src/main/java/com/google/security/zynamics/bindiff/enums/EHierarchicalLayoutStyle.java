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

public enum EHierarchicalLayoutStyle {
  PENDULUM_MULTILINE,
  PENDULUM_ORTHOGONAL,
  LINEAR_SEGMEMENTS_MULTILINE,
  LINEAR_SEGMENTS_ORTHOGONAL;

  public static EHierarchicalLayoutStyle getEnum(final int style) {
    switch (style) {
      case 0:
        return PENDULUM_MULTILINE;
      case 1:
        return PENDULUM_ORTHOGONAL;
      case 2:
        return LINEAR_SEGMEMENTS_MULTILINE;
      case 3:
        return LINEAR_SEGMENTS_ORTHOGONAL;
    }

    throw new IllegalArgumentException("Unknown hierarchical layout style.");
  }

  public static int getOrdinal(final EHierarchicalLayoutStyle style) {
    switch (style) {
      case PENDULUM_MULTILINE:
        return 0;
      case PENDULUM_ORTHOGONAL:
        return 1;
      case LINEAR_SEGMEMENTS_MULTILINE:
        return 2;
      case LINEAR_SEGMENTS_ORTHOGONAL:
        return 3;
    }

    throw new IllegalArgumentException("Unknown hierarchical layout style.");
  }
}
