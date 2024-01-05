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

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.Tokenizer;

import com.google.common.base.Preconditions;

public class CMultiCommentLineTokenizer {
  private final String m_text;

  private final String m_delimiter;

  private int m_pointer = 0;

  public CMultiCommentLineTokenizer(final String text, final String delimiter) {
    m_delimiter = Preconditions.checkNotNull(delimiter, "Error: Text delimiter can't be null.");
    Preconditions.checkArgument(!("".equals(delimiter)), "Error: Text delimiter can't be empty.");
    m_text = Preconditions.checkNotNull(text, "Error: Text can't be null.");
  }

  public boolean hasMoreTokens() {
    return m_pointer < m_text.length();
  }

  public String nextToken() {
    if (m_pointer >= m_text.length()) {
      return null;
    }

    final int startPointer = m_pointer;

    int endPointer = m_text.indexOf(m_delimiter, startPointer) + 1;

    if (endPointer == 0) {
      endPointer = m_text.length();
    }

    m_pointer = endPointer;

    String token = m_text.substring(startPointer, endPointer);

    if (!hasMoreTokens() && !token.isEmpty() && !token.endsWith("\r")) {
      token += "\r";
    }

    return token;
  }
}
