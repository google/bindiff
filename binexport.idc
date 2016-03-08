#include <idc.idc>
static main() {
  Batch(0);
  Wait();
  RunPlugin("zynamics_binexport_9", 1);
  Exit(0);
}
