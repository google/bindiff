#include "third_party/zynamics/bindiff/flow_graph_match.h"

#include <map>
#include <unordered_map>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/tinyxpath/xpath_processor.h"
#include "third_party/tinyxpath/xpath_static.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_call_refs.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_edges_lengauer_tarjan.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_edges_mdindex.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_edges_prime.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_entry_node.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_hash.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_instruction_count.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_jump_sequence.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_loop_entry.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_mdindex.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_mdindex_relaxed.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_prime.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_self_loop.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_string_refs.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"

namespace security {
namespace bindiff {
namespace {

double GetConfidenceFromConfig(const string& name) {
  return GetConfig().ReadDouble(
      absl::StrCat("/BinDiff/BasicBlockMatching/Step[@algorithm=\"", name,
                   "\"]/@confidence"),
      /*default_value=*/-1.0 /* Not found/commented out */);
}

// Special matching step of last resort. Single unmatched parents/children of
// matched basic blocks are matched - with no regard of their content.
bool MatchUnique(const VertexSet& vertices1, const VertexSet& vertices2,
                 FixedPoint& fixed_point) {
  if (vertices1.size() == 1 && vertices2.size() == 1) {
    return fixed_point.Add(*vertices1.begin(), *vertices2.begin(),
                           "basicBlock: propagation (size==1)") !=
           fixed_point.GetBasicBlockFixedPoints().end();
  }
  return false;
}

void GetUnmatchedChildren(const FlowGraph* graph, FlowGraph::Vertex vertex,
                          VertexSet* vertices) {
  vertices->clear();
  FlowGraph::OutEdgeIterator j, end;
  for (boost::tie(j, end) = boost::out_edges(vertex, graph->GetGraph());
       j != end; ++j) {
    const auto target = boost::target(*j, graph->GetGraph());
    if (!graph->GetFixedPoint(target)) {
      vertices->emplace(target);
    }
  }
}

void GetUnmatchedParents(const FlowGraph* graph, FlowGraph::Vertex vertex,
                         VertexSet* vertices) {
  vertices->clear();
  FlowGraph::InEdgeIterator j, end;
  for (boost::tie(j, end) = boost::in_edges(vertex, graph->GetGraph());
       j != end; ++j) {
    const auto source = boost::source(*j, graph->GetGraph());
    if (!graph->GetFixedPoint(source)) {
      vertices->emplace(source);
    }
  }
}

}  // namespace

void AddFlag(FlowGraph* flow_graph, const FlowGraph::Edge& edge, size_t flag) {
  const auto source = boost::source(edge, flow_graph->GetGraph());
  const auto target = boost::target(edge, flow_graph->GetGraph());
  flow_graph->SetFlags(source, flow_graph->GetFlags(source) | flag);
  flow_graph->SetFlags(target, flow_graph->GetFlags(target) | flag);
}

void FindFixedPointsBasicBlock(FixedPoint* fixed_point,
                               MatchingContext* context,
                               const MatchingStepsFlowGraph& default_steps) {
  FlowGraph* primary = fixed_point->GetPrimary();
  FlowGraph* secondary = fixed_point->GetSecondary();
  VertexSet vertices1, vertices2;
  for (MatchingStepsFlowGraph matching_steps_for_current_level = default_steps;
       !matching_steps_for_current_level.empty();
       matching_steps_for_current_level.pop_front()) {
    FlowGraph::VertexIterator j, end;
    for (boost::tie(j, end) = boost::vertices(primary->GetGraph()); j != end;
         ++j) {
      if (!primary->GetFixedPoint(*j)) {
        vertices1.emplace(*j);
      }
    }
    for (boost::tie(j, end) = boost::vertices(secondary->GetGraph()); j != end;
         ++j) {
      if (!secondary->GetFixedPoint(*j)) {
        vertices2.emplace(*j);
      }
    }
    if (vertices1.empty() || vertices2.empty()) {
      return;  // Already matched everything.
    }
    MatchingStepsFlowGraph matching_steps = matching_steps_for_current_level;
    matching_steps.front()->FindFixedPoints(primary, secondary, vertices1,
                                            vertices2, fixed_point, context,
                                            &matching_steps);
    matching_steps = matching_steps_for_current_level;

    bool more_fixed_points_discovered = false;
    do {
      more_fixed_points_discovered = false;
      BasicBlockFixedPoints& fixed_points =
          fixed_point->GetBasicBlockFixedPoints();
      // Propagate down to unmatched children.
      for (const auto& basic_block_fixed_point : fixed_points) {
        GetUnmatchedChildren(
            primary, basic_block_fixed_point.GetPrimaryVertex(), &vertices1);
        GetUnmatchedChildren(secondary,
                             basic_block_fixed_point.GetSecondaryVertex(),
                             &vertices2);
        matching_steps = matching_steps_for_current_level;
        if (!vertices1.empty() && !vertices2.empty()) {
          more_fixed_points_discovered |=
              matching_steps.front()->FindFixedPoints(
                  primary, secondary, vertices1, vertices2, fixed_point,
                  context, &matching_steps);
        }
      }

      // Propagate up to unmatched parents.
      for (const auto& basic_block_fixed_point : fixed_points) {
        GetUnmatchedParents(primary, basic_block_fixed_point.GetPrimaryVertex(),
                            &vertices1);
        GetUnmatchedParents(secondary,
                            basic_block_fixed_point.GetSecondaryVertex(),
                            &vertices2);
        matching_steps = matching_steps_for_current_level;
        if (!vertices1.empty() && !vertices2.empty()) {
          more_fixed_points_discovered |=
              matching_steps.front()->FindFixedPoints(
                  primary, secondary, vertices1, vertices2, fixed_point,
                  context, &matching_steps);
        }
      }
    } while (more_fixed_points_discovered);
  }

  bool more_fixed_points_discovered = false;
  do {
    // Last resort: Match everything that's connected to a fixed point via
    // a unique edge.
    more_fixed_points_discovered = false;
    BasicBlockFixedPoints& fixed_points =
        fixed_point->GetBasicBlockFixedPoints();
    for (const auto& basic_block_fixed_point : fixed_points) {
      // Propagate down to unmatched children.
      GetUnmatchedChildren(primary, basic_block_fixed_point.GetPrimaryVertex(),
                           &vertices1);
      GetUnmatchedChildren(
          secondary, basic_block_fixed_point.GetSecondaryVertex(), &vertices2);
      more_fixed_points_discovered |=
          MatchUnique(vertices1, vertices2, *fixed_point);
      // Propagate up to unmatched parents.
      GetUnmatchedParents(primary, basic_block_fixed_point.GetPrimaryVertex(),
                          &vertices1);
      GetUnmatchedParents(
          secondary, basic_block_fixed_point.GetSecondaryVertex(), &vertices2);
      more_fixed_points_discovered |=
          MatchUnique(vertices1, vertices2, *fixed_point);
    }
  } while (more_fixed_points_discovered);
}

MatchingStepFlowGraph::MatchingStepFlowGraph(const string& name)
    : name_(name),
      confidence_(GetConfidenceFromConfig(GetName())),
      edge_matching_(false) {}

MatchingStepsFlowGraph GetDefaultMatchingStepsBasicBlock() {
  static auto* algorithms = []() -> std::map<string, MatchingStepFlowGraph*>* {
    auto* result = new std::map<string, MatchingStepFlowGraph*>();
    // TODO(cblichmann): Add proximity md index matching.
    // TODO(cblichmann): Add relaxed and proximity edge matching.
    // TODO(cblichmann): Make it possible to disable propagation == 1 matching.
    for (auto* step : std::initializer_list<MatchingStepFlowGraph*>{
             // Edge based algorithms:
             new MatchingStepEdgesMdIndex(kTopDown),
             new MatchingStepEdgesMdIndex(kBottomUp),
             new MatchingStepEdgesPrimeProduct(),
             new MatchingStepEdgesLoop(),
             // Basic block based algorithms:
             new MatchingStepMdIndex(kTopDown),
             new MatchingStepMdIndex(kBottomUp),
             new MatchingStepHashBasicBlock(4),
             new MatchingStepPrimeBasicBlock(4),
             new MatchingStepCallReferences(),
             new MatchingStepStringReferences(),
             new MatchingStepMdIndexRelaxed(),
             new MatchingStepPrimeBasicBlock(0),
             new MatchingStepLoopEntry(),
             new MatchingStepSelfLoops(),
             new MatchingStepEntryNodes(kTopDown),
             new MatchingStepEntryNodes(kBottomUp),
             new MatchingStepInstructionCount(),
             new MatchingStepJumpSequence(),
         }) {
      (*result)[step->GetName()] = step;
    }
    return result;
  }();

  MatchingStepsFlowGraph matching_steps_basic_block;
  TinyXPath::xpath_processor processor(GetConfig().GetDocument()->RootElement(),
                                       "/BinDiff/BasicBlockMatching/Step");
  const size_t num_nodes = processor.u_compute_xpath_node_set();
  for (size_t i = 0; i < num_nodes; ++i) {
    bool is_attribute = false;
    const TiXmlBase* node = 0;
    processor.v_get_xpath_base(i, node, is_attribute);
    const string name = TinyXPath::XAp_xpath_attribute(
                            dynamic_cast<const TiXmlNode*>(node), "@algorithm")
                            ->Value();
    auto algorithm = algorithms->find(name);
    if (algorithm != algorithms->end())
      matching_steps_basic_block.push_back(algorithm->second);
  }
  if (matching_steps_basic_block.empty()) {
    throw std::runtime_error(
        "no basic block matching algorithms registered - "
        "is the config file valid?");
  }

  return matching_steps_basic_block;
}

}  // namespace bindiff
}  // namespace security
