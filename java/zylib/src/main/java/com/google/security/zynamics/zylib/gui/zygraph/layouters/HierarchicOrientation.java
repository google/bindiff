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

package com.google.security.zynamics.zylib.gui.zygraph.layouters;

public enum HierarchicOrientation {
  VERTICAL,
  HORIZONTAL;

  public static HierarchicOrientation parseInt(final int orientation) {
    if (orientation == VERTICAL.ordinal()) {
      return VERTICAL;
    } else if (orientation == HORIZONTAL.ordinal()) {
      return HORIZONTAL;
    } else {
      throw new IllegalStateException("Internal Error: Invalid orientation value");
    }
  }
}
