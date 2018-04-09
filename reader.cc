#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"

namespace security {
namespace bindiff {

bool operator<(const FixedPointInfo& one, const FixedPointInfo& two) {
  return one.primary < two.primary;
}

bool FixedPointInfo::IsManual() const {
  return confidence == 1.0 && algorithm &&
         algorithm->find("manual") != string::npos;
}

Reader::Reader() : similarity_(0.0), confidence_(0.0) {}

double Reader::GetSimilarity() const { return similarity_; }

double Reader::GetConfidence() const { return confidence_; }

}  // namespace bindiff
}  // namespace security
