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

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import java.util.List;

public interface IGroupNodeListener<NodeType, CommentType> {
  /**
   * Invoked if a comment was appended to the list of group node comments.
   *
   * @param node The group node where the comment was appended.
   * @param comment The comment which was appended.
   */
  void appendedGroupNodeComment(NodeType node, CommentType comment);

  /**
   * Invoked if a comment was deleted from the list of group node comments.
   *
   * @param node The group node where the comment was deleted.
   * @param comment The comment which was deleted.
   */
  void deletedGroupNodeComment(NodeType node, CommentType comment);

  /**
   * Invoked if a comment in the list of group node comments was edited.
   *
   * @param node The group node where the comment was edited.
   * @param comment The comment which was edited.
   */
  void editedGroupNodeComment(NodeType node, CommentType comment);

  /**
   * Invoked if the comments of a group node have been initialized.
   *
   * @param node The group node where the comments have been initialized
   * @param comment The list of comments which are now associated to the group node.
   */
  void initializedGroupNodeComment(NodeType node, List<CommentType> comment);
}
