# SQLite embedded database
add_library(sqlite STATIC ${ThirdParty_DIR}/sqlite/src/sqlite3.c)
if(UNIX AND (NOT APPLE))
  target_link_libraries(sqlite dl)
endif()

