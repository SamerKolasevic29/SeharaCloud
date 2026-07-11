cmake_minimum_required(VERSION 3.20)
project(homecloud_indexer)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(PkgConfig REQUIRED)
pkg_check_modules(LIBPQXX   REQUIRED libpqxx)
pkg_check_modules(TAGLIB    REQUIRED taglib)
pkg_check_modules(EXIV2     REQUIRED exiv2)
pkg_check_modules(POPPLER   REQUIRED poppler-cpp)
pkg_check_modules(OPENSSL   REQUIRED openssl)

add_executable(indexer
    src/db.cpp
    src/metadata.cpp
    # src/scanner.cpp   <- will uncomment when its done
    # src/watcher.cpp
    # src/main.cpp
)

target_include_directories(indexer PRIVATE
    include
    ${LIBPQXX_INCLUDE_DIRS}
    ${TAGLIB_INCLUDE_DIRS}
    ${EXIV2_INCLUDE_DIRS}
    ${POPPLER_INCLUDE_DIRS}
    ${OPENSSL_INCLUDE_DIRS}
)

target_link_libraries(indexer
    ${LIBPQXX_LIBRARIES}
    ${TAGLIB_LIBRARIES}
    ${EXIV2_LIBRARIES}
    ${POPPLER_LIBRARIES}
    ${OPENSSL_LIBRARIES}
)