cmake_minimum_required(VERSION 3.20)
project(homecloud_indexer)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find libraries
find_package(PkgConfig REQUIRED)
pkg_check_modules(LIBPQXX   REQUIRED libpqxx)

add_executable(indexer
    src/db.cpp 
)

target_include_directories(indexer PRIVATE
    include
    ${LIBPQXX_INCLUDE_DIRS}
)

target_link_libraries(indexer
    ${LIBPQXX_LIBRARIES}
)