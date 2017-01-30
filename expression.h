// Copyright 2011-2017 Google Inc. All Rights Reserved.
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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_EXPRESSION_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_EXPRESSION_H_

#include <cstdint>
#include <iosfwd>
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <vector>

#include "third_party/zynamics/binexport/types.h"

#pragma pack(push, 1)
class Expression {
 public:
  typedef std::unordered_map<std::string, Expression> ExpressionCache;
  typedef std::unordered_set<std::string> StringCache;

  enum Type : uint8_t {
    TYPE_MNEMONIC = 0,
    TYPE_SYMBOL = 1,
    TYPE_IMMEDIATE_INT = 2,
    TYPE_IMMEDIATE_FLOAT = 3,
    TYPE_OPERATOR = 4,
    TYPE_REGISTER = 5,
    TYPE_SIZEPREFIX = 6,
    TYPE_DEREFERENCE = 7,

    TYPE_NEWOPERAND = 8,
    TYPE_STACKVARIABLE = 9,
    TYPE_GLOBALVARIABLE = 10,
    TYPE_JUMPLABEL = 11,
    TYPE_FUNCTION = 12,

    TYPE_INVALID = 255
  };

  ~Expression() = default;

  bool IsSymbol() const;
  bool IsImmediate() const;
  bool IsOperator() const;
  bool IsDereferenceOperator() const;
  int GetId() const;
  Type GetType() const;
  const std::string& GetSymbol() const;
  uint16_t GetPosition() const;
  int64_t GetImmediate() const;
  const Expression* GetParent() const;
  std::string CreateSignature();
  static Expression* Create(Expression* parent, const std::string& symbol = "",
                            int64_t immediate = 0,
                            Type type = TYPE_IMMEDIATE_INT,
                            uint16_t position = 0);
  static void EmptyCache();
  static const ExpressionCache& GetExpressions();

 private:
  // The constructor is private for two reasons:
  // - We want to make sure expressions can only be created on the heap.
  // - We want to avoid creating duplicate expressions so we just
  //   refer to the expression_cache_ if someone tries (that way archiving
  //   compression/de-duping)
  // TODO(user): What about copy & assignment?
  Expression(Expression* parent, const std::string& symbol, int64_t immediate,
             Type type, uint16_t position);
  static const std::string* CacheString(const std::string& string);

  const std::string* symbol_;
  int64_t immediate_;
  Expression* parent_;
  int id_;
  uint16_t position_;
  Type type_;

  static StringCache string_cache_;
  static ExpressionCache expression_cache_;
  static int global_id_;
};
#pragma pack(pop)

typedef std::vector<Expression*> Expressions;
std::ostream& operator<<(std::ostream& stream, const Expression& expression);

class Instruction;
std::pair<int, int> GetSourceExpressionId(const Instruction& instruction,
                                          Address target);

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_EXPRESSION_H_
