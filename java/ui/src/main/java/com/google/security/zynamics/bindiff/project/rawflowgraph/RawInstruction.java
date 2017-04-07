package com.google.security.zynamics.bindiff.project.rawflowgraph;

import com.google.common.base.Preconditions;
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

  private List<RawInstructionComment> comments;

  public RawInstruction(
      final IAddress address,
      final String mnemonic,
      final int maxMnemonicLen,
      final byte[] taggedOperandDisassembly,
      final long[] callTargetAddr,
      final List<RawInstructionComment> comments) {
    this.address = Preconditions.checkNotNull(address);
    this.mnemonic = Preconditions.checkNotNull(mnemonic);
    this.maxMnemonicLen = maxMnemonicLen;
    this.taggedOperandDisassembly = Preconditions.checkNotNull(taggedOperandDisassembly);
    this.callTargetAddresses = Preconditions.checkNotNull(callTargetAddr);
    this.comments = comments;
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
    for (final byte value : taggedOperandDisassembly) {
      if (value > EInstructionHighlighting.ENUM_ENTRY_COUNT) {
        ++counter;
      }
    }

    return counter;
  }

  public byte[] getOperands() {
    return taggedOperandDisassembly;
  }

  public boolean hasComments() {
    return comments != null && comments.size() > 0;
  }

  public boolean isCall() {
    return callTargetAddresses.length > 0;
  }

  public void setComment(final String text, final ECommentPlacement commentPlacement) {
    if (comments != null) {
      for (final RawInstructionComment comment : comments) {
        if (comment.getPlacement() == commentPlacement) {
          comment.setText(text);

          if ("".equals(comment.getText())) {
            comments.remove(comment);

            if (comments.size() == 0) {
              comments = null;
            }
          }

          return;
        }
      }
    } else if (!"".equals(text)) {
      comments.clear();
      final RawInstructionComment comment = new RawInstructionComment(text, commentPlacement);
      comments.add(comment);
    }
  }
}
