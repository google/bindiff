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

package com.google.security.zynamics.zylib.types.graphs.algorithms;

/**
 * Exception that is used to signal that there is something wrong with the graph.
 */
public class MalformedGraphException extends Exception {
  private static final long serialVersionUID = 7422498674681635996L;

  /**
   * Creates a new MalformedGraphException object.
   * 
   * @param msg The exception message.
   */
  public MalformedGraphException(final String msg) {
    super(msg);
  }
}
