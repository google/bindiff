#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/callgraph.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flowgraph.h"

bool operator<(const FixedPointInfo& one, const FixedPointInfo& two) {
  return one.primary < two.primary;
}

bool FixedPointInfo::IsManual() const {
  return confidence == 1.0 && algorithm &&
         algorithm->find("manual") != std::string::npos;
}

Reader::Reader() : similarity_(0.0), confidence_(0.0) {}

double Reader::GetSimilarity() const { return similarity_; }

double Reader::GetConfidence() const { return confidence_; }

