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

package com.google.security.zynamics.zylib.gui.zygraph.layouters;

public enum HierarchicStyle {
  OCTLINEAR_OPTIMAL,
  ORTHOGONAL_OPTIMAL,
  POLYLINE_OPTIMAL,
  OCTLINEAR_TOPMOST,
  ORTHOGONAL_TOPMOST,
  POLYLINE_TOPMOST,
  OCTLINEAR_TIGHT_TREE,
  ORTHOGONAL_TIGHT_TREE,
  POLYLINE_TIGHT_TREE,
  OCTLINEAR_BFS,
  ORTHOGONAL_BFS,
  POLYLINE_BFS;

  public static HierarchicStyle parseInt(final int style) {
    switch (style) {
      case 0:
        return OCTLINEAR_OPTIMAL;
      case 1:
        return ORTHOGONAL_OPTIMAL;
      case 2:
        return POLYLINE_OPTIMAL;
      case 3:
        return OCTLINEAR_TOPMOST;
      case 4:
        return ORTHOGONAL_TOPMOST;
      case 5:
        return POLYLINE_TOPMOST;
      case 6:
        return OCTLINEAR_TIGHT_TREE;
      case 7:
        return ORTHOGONAL_TIGHT_TREE;
      case 8:
        return POLYLINE_TIGHT_TREE;
      case 9:
        return OCTLINEAR_BFS;
      case 10:
        return ORTHOGONAL_BFS;
      case 11:
        return POLYLINE_BFS;
      default:
        throw new IllegalStateException("Error: Invalid style");
    }
  }
}
