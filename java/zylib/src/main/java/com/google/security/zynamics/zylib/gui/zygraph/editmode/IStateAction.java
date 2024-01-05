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

package com.google.security.zynamics.zylib.gui.zygraph.editmode;

import java.awt.event.MouseEvent;

/**
 * Interface for all objects to be used as default actions that are executed as soon as a state
 * change was triggered.
 *
 * @param <T> The type of the state change object.
 */
public interface IStateAction<T> {
  /**
   * Executes an action.
   *
   * @param state The new state.
   * @param event The event that led to the new state.
   */
  void execute(T state, MouseEvent event);
}
