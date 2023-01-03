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

package com.google.security.zynamics.zylib.io;

import java.io.File;

/** Interface to be implemented by objects that want to traverse through directories. */
public interface IDirectoryTraversalCallback {
  /**
   * Called when a new directory is entered.
   *
   * @param directory The directory to be entered.
   */
  void entering(File directory);

  /**
   * Called when the current directory is left.
   *
   * @param directory The current directory.
   */
  void leaving(File directory);

  /**
   * Called on each file that is found during the directory traversal.
   *
   * @param file The next file that was found inside the current directory.
   */
  void nextFile(File file);
}
