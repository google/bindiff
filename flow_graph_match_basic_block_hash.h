#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_HASH_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_HASH_H_

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

// Matches basic blocks based on a binary hash of their raw bytes. Only used on
// basic blocks with a minimum number of specified instructions.
class MatchingStepHashBasicBlock : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepHashBasicBlock(int min_instructions)
      : MatchingStepFlowGraph(
            absl::StrCat("basicBlock: hash matching (", min_instructions,
                         " instructions minimum)"),
            absl::StrCat("Basic Block: Hash (", min_instructions,
                         " Instructions Minimum)")),
        min_instructions_(min_instructions) {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksByHash(const FlowGraph* flow_graph,
                                     const VertexSet& vertices,
                                     VertexIntMap* basic_blocks_map);

  int min_instructions_;
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_HASH_H_
