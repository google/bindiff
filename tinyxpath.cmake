# TinyXPath, used for the XmlConfig class
add_library(tinyxpath STATIC ${ThirdParty_DIR}/tinyxpath/action_store.cpp
                             ${ThirdParty_DIR}/tinyxpath/htmlutil.cpp
                             ${ThirdParty_DIR}/tinyxpath/lex_util.cpp
                             ${ThirdParty_DIR}/tinyxpath/main.cpp
                             ${ThirdParty_DIR}/tinyxpath/node_set.cpp
                             ${ThirdParty_DIR}/tinyxpath/tinystr.cpp
                             ${ThirdParty_DIR}/tinyxpath/tinyxml.cpp
                             ${ThirdParty_DIR}/tinyxpath/tinyxmlerror.cpp
                             ${ThirdParty_DIR}/tinyxpath/tinyxmlparser.cpp
                             ${ThirdParty_DIR}/tinyxpath/tokenlist.cpp
                             ${ThirdParty_DIR}/tinyxpath/xml_util.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_expression.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_processor.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_stack.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_static.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_stream.cpp
                             ${ThirdParty_DIR}/tinyxpath/xpath_syntax.cpp)
