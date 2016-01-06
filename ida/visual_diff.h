#ifndef IDA_VISUAL_DIFF_H_
#define IDA_VISUAL_DIFF_H_

#include <string>

bool SendGuiMessage(const int retries, const std::string& gui_dir,
                    const std::string& server, const unsigned short port,
                    const std::string& arguments, void progress_callback(void));

#endif  // VISUAL_DIFF_H_
