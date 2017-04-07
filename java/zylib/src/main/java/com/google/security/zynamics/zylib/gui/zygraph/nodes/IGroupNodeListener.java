// Copyright 2011 Google Inc. All Rights Reserved.

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
