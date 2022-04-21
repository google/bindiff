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

import java.util.List;

public interface IFunctionListener<CommentType> {

  /**
   * Invoked if a new comment has been added to the list of comments currently associated with this
   * function.
   * 
   * @param function The function where the comment has been appended.
   * @param comment The comment which has been appended.
   */
  void appendedComment(IFunction function, CommentType comment);

  /**
   * Invoked if the description of a function has changed.
   * 
   * @param function The function where the description has changed.
   * @param description The description which changed.
   */
  void changedDescription(IFunction function, String description);

  /**
   * Invoked if the name of a function has been changed.
   * 
   * @param function The function whose name has been changed.
   * @param name The name which has been changed.
   */
  void changedName(IFunction function, String name);

  /**
   * Invoked if the function to which this function resolves to has been changed.
   * 
   * @param function
   */
  void changedForwardedFunction(IFunction function);

  /**
   * Invoked if the function has been closed.
   * 
   * @param function The function which has been closed.
   */
  void closed(IFunction function);

  /**
   * Invoked if a comment has been deleted from the list of comments associated with this function.
   * 
   * @param function The function where this comment was deleted.
   * @param comment The comment that has been deleted.
   */
  void deletedComment(IFunction function, CommentType comment);

  /**
   * Invoked if a comment in the list of comments associated with this function has been edited.
   * 
   * @param function The function where the comment was edited.
   * @param comment The comment which was edited.
   */
  void editedComment(IFunction function, CommentType comment);

  void initializedComment(IFunction function, List<CommentType> comment);

  /**
   * Invoked if a function has been loaded.
   * 
   * @param function The function which has been loaded.
   */
  void loadedFunction(IFunction function);
}
