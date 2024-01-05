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

package com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import java.awt.Cursor;

public class CMouseCursorHelper {
  private static Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
  private static Cursor MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  private static Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

  public static void setDefaultCursor(final AbstractZyGraph<?, ?> graph) {
    if (graph.getViewCursor() != DEFAULT_CURSOR) {
      graph.setViewCursor(DEFAULT_CURSOR);
    }
  }

  public static void setHandCursor(final AbstractZyGraph<?, ?> graph) {
    if (graph.getViewCursor() != HAND_CURSOR) {
      graph.setViewCursor(HAND_CURSOR);
    }
  }

  public static void setMoveCursor(final AbstractZyGraph<?, ?> graph) {
    if (graph.getViewCursor() != MOVE_CURSOR) {
      graph.setViewCursor(MOVE_CURSOR);
    }
  }
}
