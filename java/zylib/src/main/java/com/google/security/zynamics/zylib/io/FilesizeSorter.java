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

package com.google.security.zynamics.zylib.io;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

public class FilesizeSorter implements Comparator<File>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7060651903531011219L;

  @Override
  public int compare(final File lhs, final File rhs) {
    return Long.compare(lhs.length(), rhs.length());
  }
}
