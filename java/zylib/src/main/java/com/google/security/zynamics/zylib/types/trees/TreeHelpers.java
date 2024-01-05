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

package com.google.security.zynamics.zylib.types.trees;

/** Provides small helper functions for working with tree structures defined in this package. */
public class TreeHelpers {
  private TreeHelpers() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Tests whether a given node is an ancestor node of another node.
   *
   * @param node The node to search for.
   * @param parent The parent node where the search begins.
   * @return True, if the node is an ancestor of parent. False, otherwise.
   */
  public static boolean isAncestor(final ITreeNode<?> node, final ITreeNode<?> parent) {
    if (node == null) {
      return false;
    } else if (parent == node) {
      return true;
    } else {
      return isAncestor(node.getParent(), parent);
    }
  }
}
