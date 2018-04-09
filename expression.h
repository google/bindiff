#ifndef EXPRESSION_H_
#define EXPRESSION_H_

#include <list>
#include <string>

#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

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
  string symbol_;
  std::list<Expression*> children_;
};

}  // namespace bindiff
}  // namespace security

#endif  // EXPRESSION_H_
