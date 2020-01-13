#include "third_party/zynamics/bindiff/test_util.h"

#include "third_party/zynamics/bindiff/config.h"

namespace security::bindiff {

void ApplyDefaultConfigForTesting() {
  GetConfig()->LoadFromString(R"raw(<?xml version="1.0"?>
<bindiff config-version="6">
  <function-matching>
    <!-- <step confidence="1.0" algorithm="function: name hash matching" /> -->
    <step confidence="1.0" algorithm="function: hash matching" />
    <step confidence="1.0" algorithm="function: edges flowgraph MD index" />
    <step confidence="0.9" algorithm="function: edges callgraph MD index" />
    <step confidence="0.9" algorithm="function: MD index matching (flowgraph MD index, top down)" />
    <step confidence="0.9" algorithm="function: MD index matching (flowgraph MD index, bottom up)" />
    <step confidence="0.9" algorithm="function: prime signature matching" />
    <step confidence="0.8" algorithm="function: MD index matching (callGraph MD index, top down)" />
    <step confidence="0.8" algorithm="function: MD index matching (callGraph MD index, bottom up)" />
    <!-- <step confidence="0.7" algorithm="function: edges proximity MD index" /> -->
    <step confidence="0.7" algorithm="function: relaxed MD index matching" />
    <step confidence="0.4" algorithm="function: instruction count" />
    <step confidence="0.4" algorithm="function: address sequence" />
    <step confidence="0.7" algorithm="function: string references" />
    <step confidence="0.6" algorithm="function: loop count matching" />
    <step confidence="0.1" algorithm="function: call sequence matching(exact)" />
    <step confidence="0.0" algorithm="function: call sequence matching(topology)" />
    <step confidence="0.0" algorithm="function: call sequence matching(sequence)" />
  </function-matching>
  <basic-block-matching>
    <step confidence="1.0" algorithm="basicBlock: edges prime product" />
    <step confidence="1.0" algorithm="basicBlock: hash matching (4 instructions minimum)" />
    <step confidence="0.9" algorithm="basicBlock: prime matching (4 instructions minimum)" />
    <step confidence="0.8" algorithm="basicBlock: call reference matching" />
    <step confidence="0.8" algorithm="basicBlock: string references matching" />
    <step confidence="0.7" algorithm="basicBlock: edges MD index (top down)" />
    <step confidence="0.7" algorithm="basicBlock: MD index matching (top down)" />
    <step confidence="0.7" algorithm="basicBlock: edges MD index (bottom up)" />
    <step confidence="0.7" algorithm="basicBlock: MD index matching (bottom up)" />
    <step confidence="0.6" algorithm="basicBlock: relaxed MD index matching" />
    <step confidence="0.5" algorithm="basicBlock: prime matching (0 instructions minimum)" />
    <step confidence="0.4" algorithm="basicBlock: edges Lengauer Tarjan dominated" />
    <step confidence="0.4" algorithm="basicBlock: loop entry matching" />
    <step confidence="0.3" algorithm="basicBlock: self loop matching" />
    <step confidence="0.2" algorithm="basicBlock: entry point matching" />
    <step confidence="0.1" algorithm="basicBlock: exit point matching" />
    <step confidence="0.0" algorithm="basicBlock: instruction count matching" />
    <step confidence="0.0" algorithm="basicBlock: jump sequence matching" />
  </basic-block-matching>
</bindiff>)raw");
}

}  // namespace security::bindiff
