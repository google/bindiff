// Copyright 2011-2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_OPERAND_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_OPERAND_H_

#include <cstdint>
#include <functional>
#include <set>
#include <string>
#include <unordered_map>

#include "third_party/zynamics/binexport/expression.h"
#include "third_party/zynamics/binexport/types.h"

#pragma pack(push, 1)
class Operand {
 public:
  typedef std::unordered_map<std::string, Operand> OperandCache;

  Expressions::iterator begin() const;
  Expressions::iterator end() const;
  Expressions::const_iterator cbegin() const;
  Expressions::const_iterator cend() const;
  uint8_t GetExpressionCount() const;
  int GetId() const;

  static Operand* CreateOperand(const Expressions& expressions);
  static void EmptyCache();
  static const OperandCache& GetOperands();
  static void PurgeCache(const std::set<int>& ids_to_keep);

 private:
  explicit Operand(const Expressions& expressions);

  uint32_t id_;
  uint32_t expression_index_;
  uint8_t expression_count_;

  static Expressions expressions_;
  static OperandCache operand_cache_;
  static uint32_t global_id_;
};
#pragma pack(pop)

typedef std::vector<Operand*> Operands;

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_OPERAND_H_
