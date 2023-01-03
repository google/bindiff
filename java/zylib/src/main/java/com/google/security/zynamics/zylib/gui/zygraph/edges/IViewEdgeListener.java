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

package com.google.security.zynamics.zylib.gui.zygraph.edges;

import java.awt.Color;

public interface IViewEdgeListener {
  void addedBend(IViewEdge<?> edge, CBend path);

  void changedColor(CViewEdge<?> edge, Color color);

  void changedSelection(IViewEdge<?> edge, boolean selected);

  void changedSourceX(CViewEdge<?> edge, double sourceX);

  void changedSourceY(CViewEdge<?> edge, double sourceY);

  void changedTargetX(CViewEdge<?> edge, double targetX);

  void changedTargetY(CViewEdge<?> edge, double targetY);

  void changedType(CViewEdge<?> edge, EdgeType type);

  void changedVisibility(IViewEdge<?> edge, boolean visibility);

  void clearedBends(IViewEdge<?> edge);

  void insertedBend(IViewEdge<?> edge, int index, CBend path);

  void removedBend(CViewEdge<?> edge, int index, CBend path);
}
