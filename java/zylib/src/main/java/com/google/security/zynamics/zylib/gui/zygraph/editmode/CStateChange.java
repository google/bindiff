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

package com.google.security.zynamics.zylib.gui.zygraph.editmode;

/** Describes a single state change from one mouse state to another mouse state. */
public final class CStateChange implements IMouseStateChange {
  /** The next mouse state. */
  private final IMouseState m_nextState;

  /** True, to chain the event to yFiles. */
  private final boolean m_yfiles;

  /**
   * Creates a new state change object.
   *
   * @param nextState The next mouse state.
   * @param yfiles True, to chain the event to yFiles.
   */
  public CStateChange(final IMouseState nextState, final boolean yfiles) {
    m_nextState = nextState;
    m_yfiles = yfiles;
  }

  @Override
  public IMouseState getNextState() {
    return m_nextState;
  }

  @Override
  public boolean notifyYFiles() {
    return m_yfiles;
  }
}
