#include "third_party/zynamics/bindiff/match_context.h"

#include <string>

#include "base/logging.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"

namespace security {
namespace bindiff {

void UpdateFixedPointConfidence(FixedPoint& fixed_point) {
  FlowGraphs flow_graphs1, flow_graphs2;
  CHECK(flow_graphs1.insert(fixed_point.GetPrimary()).second);
  CHECK(flow_graphs2.insert(fixed_point.GetSecondary()).second);
  FixedPoints fixed_points;
  CHECK(fixed_points.insert(fixed_point).second);
  Histogram histogram;
  Counts counts;
  GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points, &histogram,
                        &counts);
  Confidences confidences;
  fixed_point.SetConfidence(GetConfidence(histogram, &confidences));
  fixed_point.SetSimilarity(GetSimilarityScore(*fixed_point.GetPrimary(),
                                               *fixed_point.GetSecondary(),
                                               histogram, counts));
}

MatchingContext::MatchingContext(CallGraph& call_graph1, CallGraph& call_graph2,
                                 FlowGraphs& flow_graphs1,
                                 FlowGraphs& flow_graphs2,
                                 FixedPoints& fixed_points)
    : primary_call_graph_(call_graph1),
      secondary_call_graph_(call_graph2),
      primary_flow_graphs_(flow_graphs1),
      secondary_flow_graphs_(flow_graphs2),
      fixed_points_(fixed_points),
      new_fixed_points_() {}

MatchingContext::~MatchingContext() {
  // Cleanup for all the cached features
  for (int i = 0; i < kMaxFeature; ++i) {
    if (features_[i].destructor) {
      features_[i].destructor(features_[i].features);
    }
  }
}

XmlConfig& GetConfig() {
  static constexpr char kDefaultConfig[] = R"raw(<?xml version="1.0"?>
<BinDiff configVersion="4">
    <Gui directory="C:\Program Files\zynamics\BinDiff 5" server="127.0.0.1" port="2000" retries="20" />
    <Ida directory="C:\Program Files\IDA" executable="ida.exe" executable64="ida64.exe" />
    <Threads use="2" />
    <FunctionMatching>
        <Step confidence="1.0" algorithm="function: name hash matching" />
        <Step confidence="1.0" algorithm="function: hash matching" />
        <Step confidence="1.0" algorithm="function: edges flowgraph MD index" />
        <Step confidence="0.9" algorithm="function: edges callgraph MD index" />
        <Step confidence="0.9" algorithm="function: MD index matching (flowgraph MD index, top down)" />
        <Step confidence="0.9" algorithm="function: MD index matching (flowgraph MD index, bottom up)" />
        <Step confidence="0.9" algorithm="function: prime signature matching" />
        <Step confidence="0.8" algorithm="function: MD index matching (callGraph MD index, top down)" />
        <Step confidence="0.8" algorithm="function: MD index matching (callGraph MD index, bottom up)" />
        <!-- <Step confidence="0.7" algorithm="function: edges proximity MD index" /> -->
        <Step confidence="0.7" algorithm="function: relaxed MD index matching" />
        <Step confidence="0.4" algorithm="function: instruction count" />
        <Step confidence="0.4" algorithm="function: address sequence" />
        <Step confidence="0.7" algorithm="function: string references" />
        <Step confidence="0.6" algorithm="function: loop count matching" />
        <Step confidence="0.1" algorithm="function: call sequence matching(exact)" />
        <Step confidence="0.0" algorithm="function: call sequence matching(topology)" />
        <Step confidence="0.0" algorithm="function: call sequence matching(sequence)" />
    </FunctionMatching>
    <BasicBlockMatching>
        <Step confidence="1.0" algorithm="basicBlock: edges prime product" />
        <Step confidence="1.0" algorithm="basicBlock: hash matching (4 instructions minimum)" />
        <Step confidence="0.9" algorithm="basicBlock: prime matching (4 instructions minimum)" />
        <Step confidence="0.8" algorithm="basicBlock: call reference matching" />
        <Step confidence="0.8" algorithm="basicBlock: string references matching" />
        <Step confidence="0.7" algorithm="basicBlock: edges MD index (top down)" />
        <Step confidence="0.7" algorithm="basicBlock: MD index matching (top down)" />
        <Step confidence="0.7" algorithm="basicBlock: edges MD index (bottom up)" />
        <Step confidence="0.7" algorithm="basicBlock: MD index matching (bottom up)" />
        <Step confidence="0.6" algorithm="basicBlock: relaxed MD index matching" />
        <Step confidence="0.5" algorithm="basicBlock: prime matching (0 instructions minimum)" />
        <Step confidence="0.4" algorithm="basicBlock: edges Lengauer Tarjan dominated" />
        <Step confidence="0.4" algorithm="basicBlock: loop entry matching" />
        <Step confidence="0.3" algorithm="basicBlock: self loop matching" />
        <Step confidence="0.2" algorithm="basicBlock: entry point matching" />
        <Step confidence="0.1" algorithm="basicBlock: exit point matching" />
        <Step confidence="0.0" algorithm="basicBlock: instruction count matching" />
        <Step confidence="0.0" algorithm="basicBlock: jump sequence matching" />
    </BasicBlockMatching>
</BinDiff>)raw";

  static XmlConfig* config =
      (XmlConfig::GetDefaultFilename().empty()
           ? XmlConfig::LoadFromString(kDefaultConfig)
           : XmlConfig::LoadFromFile(XmlConfig::GetDefaultFilename()))
          .release();
  return *config;
}

std::pair<FixedPoints::iterator, bool> MatchingContext::AddFixedPoint(
    FlowGraph* primary, FlowGraph* secondary, const string& step_name) {
  if (primary->GetFixedPoint() || secondary->GetFixedPoint()) {
    // already a fixed point
    return std::make_pair(fixed_points_.end(), false);
  }
  auto insert_position =
      fixed_points_.insert(FixedPoint(primary, secondary, step_name));
  if (!insert_position.second) {
    // already a fixed point
    assert(false && "inconsistent fixed point data");
    return std::make_pair(fixed_points_.end(), false);
  }
  FixedPoint* new_fixed_point = const_cast<FixedPoint*>(
      &*insert_position.first);
  fixed_points_by_primary_[
      primary->GetEntryPointAddress()] = new_fixed_point;
  fixed_points_by_secondary_[
      secondary->GetEntryPointAddress()] = new_fixed_point;
  CHECK(new_fixed_points_.insert(new_fixed_point).second);
  primary->SetFixedPoint(new_fixed_point);
  secondary->SetFixedPoint(new_fixed_point);
  return insert_position;
}

}  // namespace bindiff
}  // namespace security
