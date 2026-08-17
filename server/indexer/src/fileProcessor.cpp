#include "../include/fileProcessor.h"
#include <iostream>
#include <fstream>
#include <iomanip>
#include <openssl/evp.h>
#include <taglib/fileref.h>
#include <taglib/audioproperties.h>

namespace fs = std::filesystem;

// initialization list
FileProcessor::FileProcessor(Database& database, const std::string& base_dir) 
    : db(database), base_path(base_dir) {}

// main method for routing via extension 
