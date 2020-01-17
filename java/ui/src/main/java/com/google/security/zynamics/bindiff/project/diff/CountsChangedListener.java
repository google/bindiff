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

package com.google.security.zynamics.bindiff.project.diff;

/**
 * Base class for change listeners that listen for changes in the number of diff entities. This
 * avoids having empty implementations scattered all over the code base.
 *
 * @author cblichmann@google.com (Christian Blichmann)
 */
public class CountsChangedListener {
  public void basicBlocksCountChanged() {
    // Do nothing by default
  }

  public void callsCountChanged() {
    // Do nothing by default
  }

  public void functionsCountChanged() {
    // Do nothing by default
  }

  public void instructionsCountsChanged() {
    // Do nothing by default
  }

  public void jumpsCountChanged() {
    // Do nothing by default
  }
}
