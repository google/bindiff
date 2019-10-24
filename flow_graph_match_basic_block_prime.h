#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_PRIME_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_PRIME_H_

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches basic blocks based on instruction prime product. Only used on basic
// blocks with a minimum number of specified instructions.
class MatchingStepPrimeBasicBlock : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepPrimeBasicBlock(int min_instructions)
      : MatchingStepFlowGraph(
            absl::StrCat("basicBlock: prime matching (", min_instructions,
                         " instructions minimum)"),
            absl::StrCat("Basic Block: Primes (", min_instructions,
                         " Instructions Minimum)")),
        min_instructions_(min_instructions) {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksByPrime(const FlowGraph* flow_graph,
                                      const VertexSet& vertices,
                                      VertexIntMap* basic_blocks_map);

  int min_instructions_;
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_PRIME_H_
