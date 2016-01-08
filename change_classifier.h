#ifndef CHANGE_CLASSIFIER_H_
#define CHANGE_CLASSIFIER_H_

#include <string>

class MatchingContext;
class FixedPoint;

typedef enum {
  CHANGE_NONE = 0,
  CHANGE_STRUCTURAL = 1 << 0,       // G-raph
  CHANGE_INSTRUCTIONS = 1 << 1,     // I-nstruction
  CHANGE_OPERANDS = 1 << 2,         // O-perand
  CHANGE_BRANCHINVERSION = 1 << 3,  // J-ump
  CHANGE_ENTRYPOINT = 1 << 4,       // E-entrypoint
  CHANGE_LOOPS = 1 << 5,            // L-oop
  CHANGE_CALLS = 1 << 6,            // C-all
  CHANGE_COUNT = 7
} ChangeType;

void ClassifyChanges(FixedPoint* fixed_point);
void ClassifyChanges(MatchingContext* context);
std::string GetChangeDescription(ChangeType change);

#endif  // CHANGE_CLASSIFIER_H_
