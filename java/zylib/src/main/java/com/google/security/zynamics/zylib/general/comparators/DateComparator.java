// Copyright 2011-2020 Google LLC
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
import java.util.Date;

public class DateComparator implements Comparator<Date>, Serializable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -846090338272302586L;

  @Override
  public int compare(final Date o1, final Date o2) {
    if (o1.before(o2)) {
      return -1;
    } else if (o1.equals(o2)) {
      return 0;
    } else {
      return 1;
    }
  }
}
