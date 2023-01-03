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

package com.google.security.zynamics.zylib.gui.zygraph.editmode.states;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitBackgroundTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.MouseEvent;

public class CBackgroundClickedLeftState<T extends ZyGraphNode<?>> implements IMouseState {
  private final CStateFactory<?, ?> m_factory;

  private final AbstractZyGraph<T, ?> m_graph;

  public CBackgroundClickedLeftState(
      final CStateFactory<?, ?> factory, final AbstractZyGraph<T, ?> graph) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
  }

  public AbstractZyGraph<T, ?> getGraph() {
    return m_graph;
  }

  @Override
  public CStateFactory<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return CHitBackgroundTransformer.exitBackground(m_factory, event, graph, this);
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }

  @Override
  public IMouseStateChange mouseReleased(
      final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }
}
