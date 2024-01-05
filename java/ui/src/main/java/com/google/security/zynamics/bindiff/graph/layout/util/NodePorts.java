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

package com.google.security.zynamics.bindiff.graph.layout.util;

import y.base.Node;
import y.base.YList;
import y.layout.PortCandidate;
import y.layout.PortCandidateSet;
import y.view.Graph2D;

public class NodePorts {
  private final YList topLeftPorts = new YList();
  private final YList topMiddlePorts = new YList();
  private final YList topRightPorts = new YList();
  private final YList bottomLeftPorts = new YList();
  private final YList bottomMiddlePorts = new YList();
  private final YList bottomRightPorts = new YList();

  private final PortCandidateSet candidateSet = new PortCandidateSet();

  public NodePorts(final Node n, final Graph2D graph) {
    final double width = graph.getWidth(n);
    final double halfWidth = width * 0.5;
    final double partWidth = graph.getWidth(n) / 3.0;
    final double halfHeight = graph.getHeight(n) * 0.5;

    // for each top position (left, right, ...) we create n.inDegree() ports which guarantees that
    // there are enough ports
    double stepWidth = partWidth / (n.inDegree() + 1);
    double xOffset = 0;
    for (int i = 0; i < n.inDegree(); i++) {
      xOffset += stepWidth;
      final PortCandidate pc =
          PortCandidate.createCandidate(xOffset - halfWidth, -halfHeight, PortCandidate.NORTH);
      topLeftPorts.add(pc);
      candidateSet.add(pc, 1);
    }
    xOffset += stepWidth;
    for (int i = 0; i < n.inDegree(); i++) {
      xOffset += stepWidth;
      final PortCandidate pc =
          PortCandidate.createCandidate(xOffset - halfWidth, -halfHeight, PortCandidate.NORTH);
      topMiddlePorts.add(pc);
      candidateSet.add(pc, 1);
    }
    xOffset += stepWidth;
    for (int i = 0; i < n.inDegree(); i++) {
      xOffset += stepWidth;
      final PortCandidate pc =
          PortCandidate.createCandidate(xOffset - halfWidth, -halfHeight, PortCandidate.NORTH);
      topRightPorts.add(pc);
      candidateSet.add(pc, 1);
    }

    // for each bottom position (left, right, ...) we create n.outDegree() ports which guarantees
    // that there are enough ports
    stepWidth = partWidth / (n.outDegree() + 1);
    xOffset = 0;
    for (int i = 0; i < n.outDegree(); i++) {
      xOffset += stepWidth;
      final PortCandidate pc =
          PortCandidate.createCandidate(xOffset - halfWidth, halfHeight, PortCandidate.SOUTH);
      bottomLeftPorts.add(pc);
      candidateSet.add(pc, 1);
    }
    xOffset += stepWidth;
    for (int i = 0; i < n.outDegree(); i++) {
      xOffset += stepWidth;
      final PortCandidate pc =
          PortCandidate.createCandidate(xOffset - halfWidth, halfHeight, PortCandidate.SOUTH);
      bottomMiddlePorts.add(pc);
      candidateSet.add(pc, 1);
    }
    xOffset += stepWidth;
    for (int i = 0; i < n.outDegree(); i++) {
      xOffset += stepWidth;
      final PortCandidate pc =
          PortCandidate.createCandidate(xOffset - halfWidth, halfHeight, PortCandidate.SOUTH);
      bottomRightPorts.add(pc);
      candidateSet.add(pc, 1);
    }
  }

  public YList getBottomLeftPorts() {
    return bottomLeftPorts;
  }

  public YList getBottomMiddlePorts() {
    return bottomMiddlePorts;
  }

  public YList getBottomRightPorts() {
    return bottomRightPorts;
  }

  public PortCandidateSet getCandideSet() {
    return candidateSet;
  }

  public YList getTopLeftPorts() {
    return topLeftPorts;
  }

  public YList getTopMiddlePorts() {
    return topMiddlePorts;
  }

  public YList getTopRightPorts() {
    return topRightPorts;
  }
}
