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

import com.google.security.zynamics.zylib.disassembly.CAddress;
import java.util.Comparator;

/** Compares two hexadecimal strings, typically memory addresses. */
public class HexStringComparator implements Comparator<String> {

  @Override
  public int compare(final String o1, final String o2) {
    return new CAddress(o1, 16).compareTo(new CAddress(o2, 16));
  }
}
