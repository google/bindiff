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

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import java.util.List;

/**
 * Listener interface which is used for changes in comments associated to code nodes.
 * 
 * @author timkornau@google.com (Tim Kornau)
 * 
 * @param <CodeNodeType> The type of the code node
 * @param <InstructionType> The type of the instruction.
 * @param <CommentType> The type of the comment.
 */
public interface ICodeNodeListener<CodeNodeType, InstructionType, CommentType> {

  /**
   * Notification that a new global code node comment has been appended.
   * 
   * @param codeNode The code node to which the comment has been appended.
   * @param comment The comment which has been appended.
   */
  void appendedGlobalCodeNodeComment(CodeNodeType codeNode, CommentType comment);

  /**
   * Notification that a new local code node comment has been appended.
   * 
   * @param codeNode The code node to which the comment has been appended.
   * @param comment The comment which has been appended.
   */
  void appendedLocalCodeNodeComment(CodeNodeType codeNode, CommentType comment);

  /**
   * Notification that a new local instruction comment has been appended within this code node.
   * 
   * @param codeNode The code node in which the instruction is located.
   * @param instruction The instruction where the comment has been appended.
   * @param comment The comment which has been appended.
   */
  void appendedLocalInstructionComment(CodeNodeType codeNode, InstructionType instruction,
      CommentType comment);

  /**
   * Notification that a global code node comment has been deleted.
   * 
   * @param codenode The code node in which the comment has been deleted.
   * @param comment The comment which has been deleted.
   */
  void deletedGlobalCodeNodeComment(CodeNodeType codenode, CommentType comment);

  /**
   * Notification that a local code node comment has been deleted.
   * 
   * @param codeNode The code node in which the comment has been deleted.
   * @param comment The comment which has been deleted.
   */
  void deletedLocalCodeNodeComment(CodeNodeType codeNode, CommentType comment);

  /**
   * Notification that a local instruction comment has been deleted.
   * 
   * @param codeNode The code node in which the instruction where the comment was deleted resides
   *        in.
   * @param instruction The instruction to which the deleted comment belonged to.
   * @param comment The comment which was deleted.
   */
  void deletedLocalInstructionComment(CodeNodeType codeNode, InstructionType instruction,
      CommentType comment);

  /**
   * Notification that a global code node comment has been edited.
   * 
   * @param codeNode The code node in which the comment was edited.
   * @param comment The comment which was edited.
   */
  void editedGlobalCodeNodeComment(CodeNodeType codeNode, CommentType comment);

  /**
   * Notification that a local code node comment has been edited.
   * 
   * @param codeNode The code node in which the comment was edited.
   * @param comment The comment which was edited.
   */
  void editedLocalCodeNodeComment(CodeNodeType codeNode, CommentType comment);

  /**
   * Notification that a local instruction comment has been edited.
   * 
   * @param codeNode The code node in which the instruction whose comment was edited resides in.
   * @param instruction The instruction to which the edited comment belongs.
   * @param comment The comment that was edited.
   */
  void editedLocalInstructionComment(CodeNodeType codeNode, InstructionType instruction,
      CommentType comment);

  /**
   * Notification that a global code node comment has been initialized.
   * 
   * @param codeNode The code node where the global comment has been initialized.
   * @param comments The list of comments with which the code nodes global comments have been
   *        initialized.
   */
  void initializedGlobalCodeNodeComment(CodeNodeType codeNode, List<CommentType> comments);

  /**
   * Notification that a local code node comment has been initialized.
   * 
   * @param codeNode The code node where the local comment has been initialized.
   * @param comments The list of comments with which the code nodes local comments have been
   *        initialized.
   */
  void initializedLocalCodeNodeComment(CodeNodeType codeNode, List<CommentType> comments);

  /**
   * Notification that a local instruction comment has been initialized.
   * 
   * @param codeNode The code node to which the instruction belongs.
   * @param instruction The instruction where the local instruction comment has been initialized.
   * @param comments The comments with which the instructions local comment has been initialized.
   */
  void initializedLocalInstructionComment(CodeNodeType codeNode, InstructionType instruction,
      List<CommentType> comments);
}
