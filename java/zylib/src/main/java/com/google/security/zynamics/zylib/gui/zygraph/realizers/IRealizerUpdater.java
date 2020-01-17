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

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

/**
 * Interface that must be implemented by all classes that want to update the content of realizers.
 * 
 * @param <NodeType>
 */
public interface IRealizerUpdater<NodeType extends ZyGraphNode<?>> {
  /**
   * Frees all allocated resources.
   */
  void dispose();

  /**
   * Regenerates the content of the realizer.
   * 
   * @param realizer The realizers whose content is updated.
   */
  void generateContent(IZyNodeRealizer realizer, ZyLabelContent content);

  /**
   * Called by the realizer to set the realizer updater.
   * 
   * @param realizer The realizer to be updated.
   */
  void setRealizer(IZyNodeRealizer realizer);
}
