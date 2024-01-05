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

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import java.util.List;

/**
 * Listener interface which is used for changes in comments associated with function nodes.
 *
 * @author timkornau@google.com (Tim Kornau)
 * @param <NodeType> The type of the node.
 * @param <CommentType> The type of the comment.
 */
public interface IFunctionNodeListener<NodeType, CommentType> extends IViewNodeListener {

  /**
   * Invoked if a comment has been appended to a function node.
   *
   * @param node The function node where the comment has been appended.
   * @param comment The comment that has been appended.
   */
  void appendedFunctionNodeComment(NodeType node, CommentType comment);

  /**
   * Invoked if a comment has been deleted from a function node.
   *
   * @param node The function node where the comment has been deleted.
   * @param comment The comment where that has been deleted.
   */
  void deletedFunctionNodeComment(NodeType node, CommentType comment);

  /**
   * Invoked if a function node comment has been edited.
   *
   * @param node The function node where the comment has been edited.
   * @param comment The comment that has been edited.
   */
  void editedFunctionNodeComment(NodeType node, CommentType comment);

  /**
   * Invoked if a function node comment has been initialized.
   *
   * @param node The function node where the comment has been initialized.
   * @param comment The comment with which the function nodes comment was initialized.
   */
  void initializedFunctionNodeComment(NodeType node, List<CommentType> comment);
}
