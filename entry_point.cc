// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "third_party/zynamics/binexport/entry_point.h"

#include "third_party/absl/log/log.h"

EntryPoint::EntryPoint(Address address, EntryPoint::Source source,
                       const int flags)
    : address_(address), source_(source), flags_(flags) {}

bool operator<(const EntryPoint& lhs, const EntryPoint& rhs) {
  return lhs.address_ < rhs.address_;
}

bool operator==(const EntryPoint& lhs, const EntryPoint& rhs) {
  return lhs.address_ == rhs.address_;
}

std::string EntryPoint::SourceToString() const {
  switch (source_) {
    case Source::CODE_FLOW:
      return "CODE_FLOW";
    case Source::CALL_TARGET:
      return "CALL_TARGET";
    case Source::JUMP_ANY:
      return "JUMP_ANY";
    case Source::JUMP_DIRECT:
      return "JUMP_DIRECT";
    case Source::JUMP_INDIRECT:
      return "JUMP_INDIRECT";
    case Source::JUMP_TABLE:
      return "JUMP_TABLE";
    case Source::JUMP_TABLE_BACKWARDS:
      return "JUMP_TABLE_BACKWARDS";
    case Source::ADDRESS_TABLE:
      return "ADDRESS_TABLE";
    case Source::RUNTIME_CALL_TARGET:
      return "RUNTIME_CALL_TARGET";
    case Source::FUNCTION_SIGNATURE:
      return "FUNCTION_SIGNATURE";
    case Source::FUNCTION_PROLOGUE:
      return "FUNCTION_PROLOGUE";
    case Source::FUNCTION_PROLOGUE_MODEL:
      return "FUNCTION_PROLOGUE_MODEL";
    case Source::FUNCTION_CHUNK:
      return "FUNCTION_CHUNK";
    case Source::ENTRY_POINT_IMAGE:
      return "ENTRY_POINT_IMAGE";
    case Source::ENTRY_POINT_FILE:
      return "ENTRY_POINT_FILE";
    case Source::PE64_EXCEPTION_INFO:
      return "PE64_EXCEPTION_INFO";
    case Source::MSIL_EXCEPTION_RECORD:
      return "MSIL_EXCEPTION_RECORD";
    case Source::GOLANG_TYPE_INFO:
      return "GOLANG_TYPE_INFO";
    default:
      LOG(QFATAL) << "Invalid entry point source: "
                  << static_cast<int>(source_);
      return "";  // Not reached
  }
}

EntryPointManager::~EntryPointManager() {
  LOG(INFO) << "Added " << count_ << " entry points from " << name_;
}

void EntryPointManager::Add(Address address, EntryPoint::Source source,
                            const int flags) {
  entry_points_->emplace_back(address, source, flags);
  ++count_;
  if (parent_) {
    ++parent_->count_;
  }
#ifdef VLOG
  VLOG(1) << "Entry point added at: " << std::hex << address
          << " source: " << entry_points_->back().SourceToString()
          << " from: " << name_;
#endif
}
