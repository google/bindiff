#include "third_party/zynamics/bindiff/flow_graph_match.h"

#include <map>
#include <unordered_map>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/tinyxpath/xpath_processor.h"
#include "third_party/tinyxpath/xpath_static.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"

namespace {

using VertexDoubleMap = std::multimap<double, FlowGraph::Vertex>;
using VertexIntMap = std::multimap<uint64_t, FlowGraph::Vertex>;
using EdgeDoubleMap = std::multimap<double, FlowGraph::Edge>;
using EdgeIntMap = std::multimap<uint64_t, FlowGraph::Edge>;

double GetConfidenceFromConfig(const string& name) {
  return GetConfig().ReadDouble(
      absl::StrCat("/BinDiff/BasicBlockMatching/Step[@algorithm=\"", name,
                   "\"]/@confidence"),
      /*default_value=*/-1.0 /* Not found/commented out */);
}

void AddFlag(FlowGraph* flow_graph, const FlowGraph::Edge& edge, size_t flag) {
  const auto source = boost::source(edge, flow_graph->GetGraph());
  const auto target = boost::target(edge, flow_graph->GetGraph());
  flow_graph->SetFlags(source, flow_graph->GetFlags(source) | flag);
  flow_graph->SetFlags(target, flow_graph->GetFlags(target) | flag);
}

template <typename EdgeMap>
void GetVertices(const FlowGraph& flow_graph, const EdgeMap& edges,
                 const typename EdgeMap::key_type key,
                 VertexSet* basic_blocks) {
  for (auto range = edges.equal_range(key); range.first != range.second;
       ++range.first) {
    const auto& edge = range.first->second;
    const auto source = boost::source(edge, flow_graph.GetGraph());
    if (!flow_graph.GetFixedPoint(source)) {
      basic_blocks->emplace(source);
    }
    const auto target = boost::target(edge, flow_graph.GetGraph());
    if (!flow_graph.GetFixedPoint(target)) {
      basic_blocks->emplace(target);
    }
  }
}

template <typename EdgeMap>
bool FindFixedPointsBasicBlockEdgeInternal(
    EdgeMap* edges1, EdgeMap* edges2, FlowGraph* flow_graph1,
    FlowGraph* flow_graph2, FixedPoint* fixed_point, MatchingContext* context,
    MatchingStepsFlowGraph* matching_steps) {
  const string name = matching_steps->front()->GetName();
  matching_steps->pop_front();
  const size_t step_index = matching_steps->size();

  bool fixed_points_discovered = false;
  for (typename EdgeMap::const_iterator i = edges1->begin();
       i != edges1->end();) {
    const typename EdgeMap::key_type key = i->first;
    const size_t count1 = edges1->count(key);
    const size_t count2 = edges2->count(key);
    if (count1 != 1 || count2 != 1) {
      if (count1 >= 1 && count2 >= 1) {
        // Mark basic block equivalence for this matching step.
        for (auto range = edges1->equal_range(key); range.first != range.second;
             ++range.first) {
          AddFlag(flow_graph1, range.first->second, 1 << step_index);
        }
        for (auto range = edges2->equal_range(key); range.first != range.second;
             ++range.first) {
          AddFlag(flow_graph2, range.first->second, 1 << step_index);
        }

        // Continue with next matching step.
        if (!matching_steps->empty()) {
          MatchingStepFlowGraph* step = matching_steps->front();
          VertexSet basic_blocks_1, basic_blocks_2;
          GetVertices(*flow_graph1, *edges1, key, &basic_blocks_1);
          GetVertices(*flow_graph2, *edges2, key, &basic_blocks_2);
          if (!basic_blocks_1.empty() && !basic_blocks_2.empty()) {
            fixed_points_discovered |= step->FindFixedPoints(
                flow_graph1, flow_graph2, basic_blocks_1, basic_blocks_2,
                fixed_point, context, matching_steps);
            matching_steps->push_front(step);
          }
        }
      }

      edges1->erase(key);
      edges2->erase(key);
      i = edges1->upper_bound(key);
      continue;
    }

    FlowGraph::Edge edge1 = i->second;
    FlowGraph::Edge edge2 = edges2->find(key)->second;
    if (fixed_point->Add(boost::source(edge1, flow_graph1->GetGraph()),
                         boost::source(edge2, flow_graph2->GetGraph()), name) !=
            fixed_point->GetBasicBlockFixedPoints().end() ||
        fixed_point->Add(boost::target(edge1, flow_graph1->GetGraph()),
                         boost::target(edge2, flow_graph2->GetGraph()), name) !=
            fixed_point->GetBasicBlockFixedPoints().end()) {
      fixed_points_discovered = true;
    }
    ++i;
  }
  return fixed_points_discovered;
}

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

template <typename VertexMap>
bool FindFixedPointsBasicBlockInternal(FlowGraph* primary, FlowGraph* secondary,
                                       VertexMap* vertices_1,
                                       VertexMap* vertices_2,
                                       FixedPoint* fixed_point,
                                       MatchingContext* context,
                                       MatchingStepsFlowGraph* matching_steps) {
  const string name = matching_steps->front()->GetName();
  matching_steps->pop_front();

  bool fix_points_discovered = false;
  for (typename VertexMap::const_iterator i = vertices_1->begin();
       i != vertices_1->end();) {
    const size_t count1 = vertices_1->count(i->first);
    const size_t count2 = vertices_2->count(i->first);
    if (count1 == 0 || count2 == 0) {
      ++i;
      continue;
    }

    if (count1 > 1 || count2 > 1) {
      const typename VertexMap::key_type key = i->first;

      // Continue with next matching step.
      if (!matching_steps->empty()) {
        MatchingStepFlowGraph* step = matching_steps->front();
        VertexSet basic_blocks_1, basic_blocks_2;
        for (auto range = vertices_1->equal_range(key);
             range.first != range.second; ++range.first) {
          basic_blocks_1.emplace(range.first->second);
        }
        for (auto range = vertices_2->equal_range(key);
             range.first != range.second; ++range.first) {
          basic_blocks_2.emplace(range.first->second);
        }
        if (!basic_blocks_1.empty() && !basic_blocks_2.empty()) {
          fix_points_discovered |= step->FindFixedPoints(
              primary, secondary, basic_blocks_1, basic_blocks_2, fixed_point,
              context, matching_steps);
          matching_steps->push_front(step);
        }
      }
      vertices_1->erase(key);
      vertices_2->erase(key);
      i = vertices_1->upper_bound(key);
      continue;
    }

    if (fixed_point->Add(i->second, vertices_2->find(i->first)->second, name) ==
        fixed_point->GetBasicBlockFixedPoints().end()) {
      ++i;
      continue;
    }

    ++i;
    fix_points_discovered = true;
  }

  return fix_points_discovered;
}

}  // namespace

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

MatchingStepFlowGraph::~MatchingStepFlowGraph() {}

const string& MatchingStepFlowGraph::GetName() const { return name_; }

double MatchingStepFlowGraph::GetConfidence() const { return confidence_; }

bool MatchingStepFlowGraph::IsEdgeMatching() const { return edge_matching_; }

class MatchingStepMdIndex : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepMdIndex(Direction direction)
      : MatchingStepFlowGraph(
            direction == kTopDown
                ? "basicBlock: MD index matching (top down)"
                : "basicBlock: MD index matching (bottom up)"),
        direction_(direction) {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexDoubleMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksByMdIndex(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksByMdIndex(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByMdIndex(const FlowGraph* flow_graph,
                                        const VertexSet& vertices,
                                        VertexDoubleMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex)) {
        basic_blocks_map->emplace(direction_ == kTopDown
                                      ? flow_graph->GetMdIndex(vertex)
                                      : flow_graph->GetMdIndexInverted(vertex),
                                  vertex);
      }
    }
  }

  Direction direction_;
};

class MatchingStepMdIndexRelaxed : public MatchingStepFlowGraph {
 public:
  MatchingStepMdIndexRelaxed()
      : MatchingStepFlowGraph("basicBlock: relaxed MD index matching") {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexDoubleMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksByMdIndexRelaxed(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksByMdIndexRelaxed(secondary, vertices2,
                                            &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByMdIndexRelaxed(
      const FlowGraph* flow_graph, const VertexSet& vertices,
      VertexDoubleMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex)) {
        basic_blocks_map->emplace(CalculateMdIndexNode(*flow_graph, vertex),
                                  vertex);
      }
    }
  }
};

class MatchingStepHashBasicBlock : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepHashBasicBlock(int min_instructions)
      : MatchingStepFlowGraph("basicBlock: hash matching (" +
                              std::to_string(min_instructions) +
                              " instructions minimum)"),
        min_instructions_(min_instructions) {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksByHash(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksByHash(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByHash(const FlowGraph* flow_graph,
                                     const VertexSet& vertices,
                                     VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex) &&
          flow_graph->GetInstructionCount(vertex) >= min_instructions_) {
        basic_blocks_map->emplace(flow_graph->GetHash(vertex), vertex);
      }
    }
  }

  int min_instructions_;
};

class MatchingStepPrimeBasicBlock : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepPrimeBasicBlock(int min_instructions)
      : MatchingStepFlowGraph("basicBlock: prime matching (" +
                              std::to_string(min_instructions) +
                              " instructions minimum)"),
        min_instructions_(min_instructions) {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksByPrime(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksByPrime(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByPrime(const FlowGraph* flow_graph,
                                      const VertexSet& vertices,
                                      VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex) &&
          flow_graph->GetInstructionCount(vertex) >= min_instructions_) {
        basic_blocks_map->emplace(flow_graph->GetPrime(vertex), vertex);
      }
    }
  }

  int min_instructions_;
};

class MatchingStepInstructionCount : public MatchingStepFlowGraph {
 public:
  MatchingStepInstructionCount()
      : MatchingStepFlowGraph("basicBlock: instruction count matching") {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksByInstructionCount(primary, vertices1,
                                              &vertex_map_1);
    GetUnmatchedBasicBlocksByInstructionCount(secondary, vertices2,
                                              &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByInstructionCount(
      const FlowGraph* flow_graph, const VertexSet& vertices,
      VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex)) {
        const uint64_t int_md_index = static_cast<uint64_t>(
            flow_graph->GetMdIndex(vertex) * 1000000000000000000ULL);
        basic_blocks_map->emplace(
            int_md_index + flow_graph->GetInstructionCount(vertex), vertex);
      }
    }
  }
};

class MatchingStepJumpSequence : public MatchingStepFlowGraph {
 public:
  MatchingStepJumpSequence()
      : MatchingStepFlowGraph("basicBlock: jump sequence matching") {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksByJumpSequence(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksByJumpSequence(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByJumpSequence(const FlowGraph* flow_graph,
                                             const VertexSet& vertices,
                                             VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    std::map<uint64_t, uint64_t> md_count;
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex)) {
        const uint64_t int_md_index = static_cast<uint64_t>(
            flow_graph->GetMdIndex(vertex) * 1000000000000000000ULL);
        basic_blocks_map->emplace(md_count[int_md_index]++ + int_md_index,
                                  vertex);
      }
    }
  }
};

class MatchingStepCallReferences : public MatchingStepFlowGraph {
 public:
  MatchingStepCallReferences()
      : MatchingStepFlowGraph("basicBlock: call reference matching") {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksByCallReference<kPrimary>(primary, vertices1,
                                                     &vertex_map_1, context);
    GetUnmatchedBasicBlocksByCallReference<kSecondary>(secondary, vertices2,
                                                       &vertex_map_2, context);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  typedef enum { kPrimary, kSecondary } FlowGraphType;

  template <FlowGraphType type>
  void GetUnmatchedBasicBlocksByCallReference(const FlowGraph* flow_graph,
                                              const VertexSet& vertices,
                                              VertexIntMap* basic_blocks_map,
                                              MatchingContext* context) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (flow_graph->GetFixedPoint(vertex)) {
        continue;
      }

      auto calls = flow_graph->GetCallTargets(vertex);
      if (calls.first == calls.second) {
        continue;
      }

      uint64_t index = 1;
      uint64_t address_feature = 0;
      for (; calls.first != calls.second; ++calls.first, ++index) {
        FixedPoint* fixed_point =
            type == kPrimary ? context->FixedPointByPrimary(*calls.first)
                             : context->FixedPointBySecondary(*calls.first);
        if (!fixed_point) {
          // If we couldn't match all vertices, clear basic block.
          address_feature = 0;
          break;
        }
        address_feature =
            index * (fixed_point->GetPrimary()->GetEntryPointAddress() +
                     fixed_point->GetSecondary()->GetEntryPointAddress());
      }
      if (address_feature) {
        basic_blocks_map->emplace(address_feature, vertex);
      }
    }
  }
};

class MatchingStepEntryNodes : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepEntryNodes(Direction direction)
      : MatchingStepFlowGraph(direction == kTopDown
                                  ? "basicBlock: entry point matching"
                                  : "basicBlock: exit point matching"),
        direction_(direction) {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksEntryPoint(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksEntryPoint(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksEntryPoint(const FlowGraph* flow_graph,
                                         const VertexSet& vertices,
                                         VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex)) {
        if ((direction_ == kTopDown &&
             boost::in_degree(vertex, flow_graph->GetGraph()) == 0) ||
            (direction_ == kBottomUp &&
             boost::out_degree(vertex, flow_graph->GetGraph()) == 0)) {
          basic_blocks_map->emplace(1, vertex);
        }
      }
    }
  }

  Direction direction_;
};

class MatchingStepLoopEntry : public MatchingStepFlowGraph {
 public:
  MatchingStepLoopEntry()
      : MatchingStepFlowGraph("basicBlock: loop entry matching") {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksLoopEntry(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksLoopEntry(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksLoopEntry(const FlowGraph* flow_graph,
                                        const VertexSet& vertices,
                                        VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    uint_fast64_t loop_index = 0;
    for (auto vertex : vertices) {
      if (flow_graph->GetFixedPoint(vertex)) {
        continue;
      }

      if (flow_graph->IsLoopEntry(vertex)) {
        basic_blocks_map->emplace(loop_index++, vertex);
      }
    }
  }
};

class MatchingStepSelfLoops : public MatchingStepFlowGraph {
 public:
  MatchingStepSelfLoops()
      : MatchingStepFlowGraph("basicBlock: self loop matching") {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksSelfLoops(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksSelfLoops(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksSelfLoops(const FlowGraph* flow_graph,
                                        const VertexSet& vertices,
                                        VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (flow_graph->GetFixedPoint(vertex)) {
        continue;
      }

      size_t count = 0;
      FlowGraph::OutEdgeIterator j, end;
      for (boost::tie(j, end) =
               boost::out_edges(vertex, flow_graph->GetGraph());
           j != end; ++j) {
        count += boost::source(*j, flow_graph->GetGraph()) ==
                 boost::target(*j, flow_graph->GetGraph());
      }
      if (count) {
        basic_blocks_map->emplace(count, vertex);
      }
    }
  }
};

class MatchingStepStringReferences : public MatchingStepFlowGraph {
 public:
  MatchingStepStringReferences()
      : MatchingStepFlowGraph("basicBlock: string references matching") {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksStringReferences(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksStringReferences(secondary, vertices2,
                                            &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksStringReferences(const FlowGraph* flow_graph,
                                               const VertexSet& vertices,
                                               VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (flow_graph->GetFixedPoint(vertex)) {
        continue;
      }

      const uint32_t hash = flow_graph->GetStringReferences(vertex);
      if (hash > 1) {
        basic_blocks_map->emplace(hash, vertex);
      }
    }
  }
};

class MatchingStepEdgesMdIndex : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepEdgesMdIndex(Direction direction)
      : MatchingStepFlowGraph(direction == kTopDown
                                  ? "basicBlock: edges MD index (top down)"
                                  : "basicBlock: edges MD index (bottom up)"),
        direction_(direction) {
    edge_matching_ = true;
  }

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    EdgeDoubleMap primary_edges, secondary_edges;
    GetUnmatchedEdgesMdIndex(*primary, vertices1, &primary_edges);
    GetUnmatchedEdgesMdIndex(*secondary, vertices2, &secondary_edges);
    return FindFixedPointsBasicBlockEdgeInternal(
        &primary_edges, &secondary_edges, primary, secondary, fixed_point,
        context, matching_steps);
  }

 private:
  void GetUnmatchedEdgesMdIndex(const FlowGraph& flow_graph,
                                const VertexSet& vertices,
                                EdgeDoubleMap* edges) {
    edges->clear();
    FlowGraph::EdgeIterator edge, end;
    for (boost::tie(edge, end) = boost::edges(flow_graph.GetGraph());
         edge != end; ++edge) {
      if (flow_graph.IsCircular(*edge)) {
        continue;
      }

      const auto source = boost::source(*edge, flow_graph.GetGraph());
      const auto target = boost::target(*edge, flow_graph.GetGraph());
      if ((flow_graph.GetFixedPoint(source) == nullptr ||
           flow_graph.GetFixedPoint(target) == nullptr) &&
          (vertices.find(source) != vertices.end() ||
           vertices.find(target) != vertices.end())) {
        edges->emplace(direction_ == kTopDown
                           ? flow_graph.GetMdIndex(*edge)
                           : flow_graph.GetMdIndexInverted(*edge),
                       *edge);
      }
    }
  }

  Direction direction_;
};

class MatchingStepEdgesPrimeProduct : public MatchingStepFlowGraph {
 public:
  MatchingStepEdgesPrimeProduct()
      : MatchingStepFlowGraph("basicBlock: edges prime product") {
    edge_matching_ = true;
  }

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    EdgeIntMap primary_edges, secondary_edges;
    GetUnmatchedEdgesPrimeProduct(*primary, vertices1, &primary_edges);
    GetUnmatchedEdgesPrimeProduct(*secondary, vertices2, &secondary_edges);
    return FindFixedPointsBasicBlockEdgeInternal(
        &primary_edges, &secondary_edges, primary, secondary, fixed_point,
        context, matching_steps);
  }

 private:
  void GetUnmatchedEdgesPrimeProduct(const FlowGraph& flow_graph,
                                     const VertexSet& vertices,
                                     EdgeIntMap* edges) {
    edges->clear();
    FlowGraph::EdgeIterator edge, end;
    for (boost::tie(edge, end) = boost::edges(flow_graph.GetGraph());
         edge != end; ++edge) {
      if (flow_graph.IsCircular(*edge)) {
        continue;
      }
      const auto source = boost::source(*edge, flow_graph.GetGraph());
      const auto target = boost::target(*edge, flow_graph.GetGraph());
      if ((flow_graph.GetFixedPoint(source) == nullptr ||
           flow_graph.GetFixedPoint(target) == nullptr) &&
          (vertices.find(source) != vertices.end() ||
           vertices.find(target) != vertices.end())) {
        const uint64_t prime =
            flow_graph.GetPrime(source) + flow_graph.GetPrime(target) + 1;
        edges->emplace(prime, *edge);
      }
    }
  }
};

class MatchingStepEdgesLoop : public MatchingStepFlowGraph {
 public:
  MatchingStepEdgesLoop()
      : MatchingStepFlowGraph("basicBlock: edges Lengauer Tarjan dominated") {
    edge_matching_ = true;
  }

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    EdgeIntMap primary_edges, secondary_edges;
    GetUnmatchedEdgesLoop(context, *primary, vertices1, &primary_edges);
    GetUnmatchedEdgesLoop(context, *secondary, vertices2, &secondary_edges);
    return FindFixedPointsBasicBlockEdgeInternal(
        &primary_edges, &secondary_edges, primary, secondary, fixed_point,
        context, matching_steps);
  }

 private:
  typedef std::vector<int> EdgeFeatures;
  typedef std::map<const FlowGraph*, EdgeFeatures> EdgesByFlowGraph;
  enum {
    kIsCircular = 1 << 0,
    kIsEdgeDominated = 1 << 1,
  };

  static void FeatureDestructor(EdgesByFlowGraph* features) { delete features; }

  void GetUnmatchedEdgesLoop(MatchingContext* context,
                             const FlowGraph& flow_graph,
                             const VertexSet& vertices, EdgeIntMap* edges) {
    const MatchingContext::FeatureId feature_id =
        MatchingContext::kEdgeProperies;
    EdgesByFlowGraph* cached = nullptr;
    if (context->HasCachedFeatures(feature_id)) {
      cached = context->GetCachedFeatures<EdgesByFlowGraph*>(feature_id);
      if (!cached) {
        cached = new EdgesByFlowGraph();
        context->SetCachedFeatures(feature_id, cached, FeatureDestructor);
      }
    }
    edges->clear();
    FlowGraph::EdgeIterator edge, end;
    EdgeFeatures* features = nullptr;
    if (cached) {
      auto result = cached->emplace(&flow_graph, EdgeFeatures());
      // We previously cached data for this flow graph, if it's already present
      // in the map.
      features = &result.first->second;
      if (!result.second) {
        // Element wasn't added, features list is filled in.
        int edge_index = 0;
        for (boost::tie(edge, end) = boost::edges(flow_graph.GetGraph());
             edge != end; ++edge, ++edge_index) {
          int edge_feature = (*features)[edge_index];
          if (((edge_feature & kIsCircular) != 0) ||
              ((edge_feature & kIsEdgeDominated) == 0)) {
            continue;
          }
          const auto source = boost::source(*edge, flow_graph.GetGraph());
          const auto target = boost::target(*edge, flow_graph.GetGraph());
          if ((flow_graph.GetFixedPoint(source) == nullptr ||
               flow_graph.GetFixedPoint(target) == nullptr) &&
              (vertices.count(source) > 0 || vertices.count(target) > 0)) {
            edges->emplace(1, *edge);
          }
        }
        return;
      }
    }
    // Non-cached version, also fills cache, if needed.
    for (boost::tie(edge, end) = boost::edges(flow_graph.GetGraph());
         edge != end; ++edge) {
      int edge_feature = 0;
      edge_feature |= flow_graph.IsCircular(*edge) ? kIsCircular : 0;
      edge_feature |=
          (flow_graph.GetFlags(*edge) & FlowGraph::EDGE_DOMINATED) != 0
              ? kIsEdgeDominated
              : 0;
      if (features) {
        features->emplace_back(edge_feature);
      }
      if (((edge_feature & kIsCircular) != 0) ||
          ((edge_feature & kIsEdgeDominated) == 0)) {
        continue;
      }

      const auto source = boost::source(*edge, flow_graph.GetGraph());
      const auto target = boost::target(*edge, flow_graph.GetGraph());
      if (!flow_graph.IsCircular(*edge) &&
          (flow_graph.GetFixedPoint(source) == nullptr ||
           flow_graph.GetFixedPoint(target) == nullptr) &&
          (vertices.find(source) != vertices.end() ||
           vertices.find(target) != vertices.end()) &&
          (flow_graph.GetFlags(*edge) & FlowGraph::EDGE_DOMINATED)) {
        edges->emplace(1, *edge);
      }
    }
  }
};

MatchingStepsFlowGraph GetDefaultMatchingStepsBasicBlock() {
  // Edge based algorithms:
  static MatchingStepEdgesMdIndex
      matching_step_basic_block_md_index_edge_top_down(kTopDown);
  static MatchingStepEdgesMdIndex
      matching_step_basic_block_md_index_edge_bottom_up(kBottomUp);
  static MatchingStepEdgesPrimeProduct matching_step_basic_block_prime_edge;
  static MatchingStepEdgesLoop matching_step_basic_block_loop_edge;

  // Basic block based algorithms:
  static MatchingStepMdIndex matching_step_basic_block_md_index_top_down(
      kTopDown);
  static MatchingStepMdIndex matching_step_basic_block_md_index_bottom_up(
      kBottomUp);
  static MatchingStepHashBasicBlock matching_step_basic_block_hash(4);
  static MatchingStepPrimeBasicBlock matching_step_basic_block_prime_good(4);
  static MatchingStepCallReferences matching_step_basic_block_call_references;
  static MatchingStepStringReferences
      matching_step_basic_block_string_references;
  static MatchingStepMdIndexRelaxed matching_step_basic_block_md_index_relaxed;
  static MatchingStepPrimeBasicBlock matching_step_basic_block_prime(0);
  static MatchingStepLoopEntry matching_step_basic_block_loop_entry;
  static MatchingStepSelfLoops matching_step_basic_block_self_loops;
  static MatchingStepEntryNodes matching_step_basic_block_entry_nodes(kTopDown);
  static MatchingStepEntryNodes matching_step_basic_block_exit_nodes(kBottomUp);
  static MatchingStepInstructionCount
      matching_step_basic_block_instruction_count;
  static MatchingStepJumpSequence matching_step_basic_block_jump_sequence;

  typedef std::map<string, MatchingStepFlowGraph*> Algorithms;
  static Algorithms algorithms([]() -> Algorithms {
    // TODO(cblichmann): Replace lambda with initializer list once we move to
    //                   VC 2013.
    Algorithms a;
    a[matching_step_basic_block_md_index_edge_top_down.GetName()] =
        &matching_step_basic_block_md_index_edge_top_down;
    a[matching_step_basic_block_md_index_edge_bottom_up.GetName()] =
        &matching_step_basic_block_md_index_edge_bottom_up;
    a[matching_step_basic_block_prime_edge.GetName()] =
        &matching_step_basic_block_prime_edge;
    a[matching_step_basic_block_loop_edge.GetName()] =
        &matching_step_basic_block_loop_edge;
    a[matching_step_basic_block_md_index_top_down.GetName()] =
        &matching_step_basic_block_md_index_top_down;
    a[matching_step_basic_block_md_index_bottom_up.GetName()] =
        &matching_step_basic_block_md_index_bottom_up;
    a[matching_step_basic_block_hash.GetName()] =
        &matching_step_basic_block_hash;
    a[matching_step_basic_block_prime_good.GetName()] =
        &matching_step_basic_block_prime_good;
    a[matching_step_basic_block_call_references.GetName()] =
        &matching_step_basic_block_call_references;
    a[matching_step_basic_block_string_references.GetName()] =
        &matching_step_basic_block_string_references;
    a[matching_step_basic_block_md_index_relaxed.GetName()] =
        &matching_step_basic_block_md_index_relaxed;
    a[matching_step_basic_block_prime.GetName()] =
        &matching_step_basic_block_prime;
    a[matching_step_basic_block_loop_entry.GetName()] =
        &matching_step_basic_block_loop_entry;
    a[matching_step_basic_block_self_loops.GetName()] =
        &matching_step_basic_block_self_loops;
    a[matching_step_basic_block_entry_nodes.GetName()] =
        &matching_step_basic_block_entry_nodes;
    a[matching_step_basic_block_exit_nodes.GetName()] =
        &matching_step_basic_block_exit_nodes;
    a[matching_step_basic_block_instruction_count.GetName()] =
        &matching_step_basic_block_instruction_count;
    a[matching_step_basic_block_jump_sequence.GetName()] =
        &matching_step_basic_block_jump_sequence;
    return a;
  }());
  // TODO(soerenme) Add proximity md index matching.
  // TODO(soerenme) Add relaxed and proximity edge matching.
  // TODO(soerenme) Make it possible to disable propagation == 1 matching.

  MatchingStepsFlowGraph matching_steps_basic_block;
  TinyXPath::xpath_processor processor(GetConfig().GetDocument()->RootElement(),
                                       "/BinDiff/BasicBlockMatching/Step");
  const size_t num_nodes = processor.u_compute_xpath_node_set();
  for (size_t i = 0; i < num_nodes; ++i) {
    bool is_attribute = false;
    const TiXmlBase* node = 0;
    processor.v_get_xpath_base(i, node, is_attribute);
    const string name =
        TinyXPath::XAp_xpath_attribute(dynamic_cast<const TiXmlNode*>(node),
                                       "@algorithm")
            ->Value();
    Algorithms::iterator algorithm = algorithms.find(name);
    if (algorithm != algorithms.end())
      matching_steps_basic_block.push_back(algorithm->second);
  }
  if (matching_steps_basic_block.empty()) {
    throw std::runtime_error(
        "no basic block matching algorithms registered - "
        "is the config file valid?");
  }

  return matching_steps_basic_block;
}
