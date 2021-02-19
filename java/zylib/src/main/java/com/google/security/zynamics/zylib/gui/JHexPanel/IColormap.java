// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.gui.JHexPanel;

import java.awt.Color;

public interface IColormap {
  /**
   * Determines whether the byte at the given offset should be colored or not.
   * 
   * @param data The data array that can be used to determine the return value.
   * @param currentOffset The offset of the byte in question.
   * 
   * @return True if the byte should be colored. False, otherwise.
   */
  boolean colorize(final byte[] data, final long currentOffset);

  /**
   * Returns the background color that should be used to color the byte at the given offset.
   * 
   * @param data The data array that can be used to determine the return value.
   * @param currentOffset The offset of the byte in question.
   * 
   * @return The background color to be used by that byte. Null, if the default background color
   *         should be used,
   */
  Color getBackgroundColor(final byte[] data, final long currentOffset);

  /**
   * Returns the foreground color that should be used to color the byte at the given offset.
   * 
   * @param data The data array that can be used to determine the return value.
   * @param currentOffset The offset of the byte in question.
   * 
   * @return The foreground color to be used by that byte. Null, if the default foreground color
   *         should be used,
   */
  Color getForegroundColor(final byte[] data, final long currentOffset);
}
