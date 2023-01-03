// Copyright 2011-2023 Google LLC
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

#ifndef EXPRESSION_H_
#define EXPRESSION_H_

#include <list>
#include <string>

#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

class Expression {
 public:
  enum Type {
    TYPE_MNEMONIC = 0,
    TYPE_SYMBOL = 1,
    TYPE_IMMEDIATE_INT = 2,
    TYPE_IMMEDIATE_FLOAT = 3,
    TYPE_OPERATOR = 4,
    TYPE_REGISTER = 5,
    TYPE_SIZEPREFIX = 6,
    TYPE_DEREFERENCE = 7
  };

  Expression();

 private:
  Type type_;
  Address immediate_;
  std::string symbol_;
  std::list<Expression*> children_;
};

}  // namespace security::bindiff

#endif  // EXPRESSION_H_
