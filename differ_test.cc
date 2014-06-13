#include "gtest/gtest.h"

int main(int argument_count, char** arguments) {
  ::testing::InitGoogleTest(&argument_count, arguments);
  return RUN_ALL_TESTS();
}
