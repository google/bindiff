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

package com.google.security.zynamics.zylib.general.comparators;

import java.io.Serializable;
import java.util.Comparator;

public class IntComparator implements Comparator<Integer>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -9039204490352575348L;

  @Override
  public int compare(final Integer o1, final Integer o2) {
    return o1 - o2;
  }
}
