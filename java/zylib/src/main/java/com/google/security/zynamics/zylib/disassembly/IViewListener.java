// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.zylib.disassembly;

import java.util.Date;

@SuppressWarnings("hiding")
public interface IViewListener<ViewType> {
  /**
   * Invoked when the description of a view is changed.
   * 
   * @param view The view where the description is changed.
   * @param description The description which is the new view description.
   */
  void changedDescription(ViewType view, String description);

  /**
   * Invoked when the modification date of a view is changed.
   * 
   * @param view The view whose modification date is changed.
   * @param modificationDate The new modification date.
   */
  void changedModificationDate(ViewType view, Date modificationDate);

  /**
   * Invoked if the name of a view has been changed.
   * 
   * @param view The view whose name has been changed.
   * @param name The new name of the view.
   */
  void changedName(ViewType view, String name);

  /**
   * Invoked if the view is in the closing state.
   * 
   * @return true = view can be closed.
   */
  boolean closingView(ViewType view);

  /**
   * Invoked if a view has been loaded.
   * 
   * @param view The view that has just been loaded.
   */
  void loadedView(ViewType view);
}
