package com.google.security.zynamics.bindiff.project.rawcallgraph;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.graph.edges.CombinedViewEdge;

public class RawCombinedCall extends CombinedViewEdge<RawCombinedFunction> {
  private final RawCall primaryCall;
  private final RawCall secondaryCall;

  public RawCombinedCall(
      final RawCombinedFunction source,
      final RawCombinedFunction target,
      final RawCall primaryCall,
      final RawCall secondaryCall) {
    super(source, target);
    this.primaryCall = primaryCall;
    this.secondaryCall = secondaryCall;
  }

  @Override
  public EMatchState getMatchState() {
    if (primaryCall == null) {
      return EMatchState.SECONDRAY_UNMATCHED;
    }
    if (secondaryCall == null) {
      return EMatchState.PRIMARY_UNMATCHED;
    }
    return EMatchState.MATCHED;
  }

  @Override
  public RawCall getPrimaryEdge() {
    return primaryCall;
  }

  @Override
  public RawCall getSecondaryEdge() {
    return secondaryCall;
  }

  @Override
  public RawCombinedFunction getSource() {
    return super.getSource();
  }

  @Override
  public RawCombinedFunction getTarget() {
    return super.getTarget();
  }

  public boolean isChanged() {
    // NOTE: If this is a changed call the getMatchState() function's return value is !=
    // EMatchState.MATCHED!
    if (getMatchState() != EMatchState.MATCHED) {
      if (primaryCall != null) {
        return primaryCall.isChanged();
      }

      if (secondaryCall != null) {
        return secondaryCall.isChanged();
      }
    }

    return false;
  }
}
