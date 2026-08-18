cmake_minimum_required(VERSION 3.10)
project(SeharaCloudIndexer)

# C++17 for std::filesystem i std::optional
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

# using pkg-config for finding libs
find_package(PkgConfig REQUIRED)

# finding ImageMagick, TagLib and Poppler
pkg_check_modules(MAGICK REQUIRED Magick++)
pkg_check_modules(TAGLIB REQUIRED taglib)
pkg_check_modules(POPPLER REQUIRED poppler-cpp)

# OpenSSL has built-in support in CMake
find_package(OpenSSL REQUIRED)

# Defining exe
set(SOURCES
    src/main.cpp
    src/db.cpp
    src/fileProcessor.cpp
    src/watcher.cpp
)

add_executable(indexer_daemon ${SOURCES})

target_compile_options(indexer_daemon PRIVATE ${MAGICK_CFLAGS_OTHER})

# Include dirs
target_include_directories(indexer_daemon PUBLIC
    src
    ${MAGICK_INCLUDE_DIRS}
    ${TAGLIB_INCLUDE_DIRS}
    ${POPPLER_INCLUDE_DIRS}
    ${OPENSSL_INCLUDE_DIR}
)

# linking binaries (Link Libraries)
# libpqxx and libpq (PostgreSQL) usualy works directly via Linux calls
target_link_libraries(indexer_daemon
    ${MAGICK_LIBRARIES}
    ${TAGLIB_LIBRARIES}
    ${POPPLER_LIBRARIES}
    OpenSSL::Crypto
    pqxx
    pq
    stdc++fs
)