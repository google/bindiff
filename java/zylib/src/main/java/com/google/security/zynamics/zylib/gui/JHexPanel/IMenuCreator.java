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

package com.google.security.zynamics.zylib.gui.JHexPanel;

import javax.swing.JPopupMenu;

/**
 * This interface must be implemented by all classes that want to provide context menus for the
 * JHexView control.
 */
public interface IMenuCreator {

  /**
   * This function is called to generate a popup menu after the user right-clicked somewhere in the
   * hex control.
   *
   * @param offset The offset of the right-click.
   * @return The popup menu suitable for that offset or null if no popup menu should be shown.
   */
  JPopupMenu createMenu(long offset);
}
