// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.project.rawflowgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EInstructionHighlighting;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import java.util.List;

public class RawInstruction {
  private final IAddress address;

  private final String mnemonic;
  private final int maxMnemonicLen;
  private final byte[] taggedOperandDisassembly;
  private final long[] callTargetAddresses;

  private final List<RawInstructionComment> comments;

  public RawInstruction(
      final IAddress address,
      final String mnemonic,
      final int maxMnemonicLen,
      final byte[] taggedOperandDisassembly,
      final long[] callTargetAddr,
      final List<RawInstructionComment> comments) {
    this.address = checkNotNull(address);
    this.mnemonic = checkNotNull(mnemonic);
    this.maxMnemonicLen = maxMnemonicLen;
    this.taggedOperandDisassembly = checkNotNull(taggedOperandDisassembly);
    this.callTargetAddresses = checkNotNull(callTargetAddr);
    this.comments = checkNotNull(comments);
  }

  public IAddress getAddress() {
    return address;
  }

  public long[] getCallTargetAddresses() {
    return callTargetAddresses;
  }

  public List<RawInstructionComment> getComments() {
    return comments;
  }

  public String getMnemonic() {
    return mnemonic;
  }

  public int getMaxMnemonicLen() {
    return maxMnemonicLen;
  }

  public int getOperandLength() {
    int counter = 0;
    for (final byte b : taggedOperandDisassembly) {
      if (!EInstructionHighlighting.validOrdinal(b)) {
        ++counter;
      }
    }
    return counter;
  }

  public byte[] getOperands() {
    return taggedOperandDisassembly;
  }

  public boolean hasComments() {
    return !comments.isEmpty();
  }

  public boolean isCall() {
    return callTargetAddresses.length > 0;
  }

  public void setComment(final String text, final ECommentPlacement commentPlacement) {
    if (!comments.isEmpty()) {
      if (text.isEmpty()) {
        comments.removeIf(comment -> comment.getPlacement() == commentPlacement);
      } else {
        for (final RawInstructionComment comment : comments) {
          if (comment.getPlacement() == commentPlacement) {
            comment.setText(text);
            break;
          }
        }
      }
    } else if (!"".equals(text)) {
      comments.add(new RawInstructionComment(text, commentPlacement));
    }
  }
}
