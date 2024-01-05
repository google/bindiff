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

package com.google.security.zynamics.zylib.general.comparators;

import java.io.Serializable;
import java.util.Comparator;
import javax.swing.JCheckBox;

public class JCheckBoxComparator implements Comparator<JCheckBox>, Serializable {
  /** Used for serialization. */
  private static final long serialVersionUID = -2526854370340524821L;

  @Override
  public int compare(final JCheckBox o1, final JCheckBox o2) {
    return Boolean.valueOf(o1.isSelected()).compareTo(Boolean.valueOf(o2.isSelected()));
  }
}
