#ifndef FILEPROCESSOR_H
#define FILEPROCESSOR_H

#include <string>
#include <filesystem>
#include <optional>
#include "db.h"

class FileProcessor {
private:
    Database& db;
    std::string physical_base; // for mainpulating with data
    std::string logical_base;  // for inserting metadata

    // Helpers for paths
    struct PathResult {
        std::string physical_target;
        std::string logical_target;
    };

    PathResult preparePaths(const std::string& subfolder, const std::string& filename);

    // helper methods
    std::string calculateSHA256(const std::filesystem::path& filepath);
    std::string determineMimeType(const std::string& extension);
    std::string execCommand(const std::string& cmd); // for ffprobe

    // methods for metadata extraction for each file type
    bool processMusic(const std::filesystem::path& staging_path, const std::string& filename);    
    bool processVideo(const std::filesystem::path& staging_path, const std::string& filename);    
    bool processDocument(const std::filesystem::path& staging_path, const std::string& filename);
    bool processImage(const std::filesystem::path& staging_path, const std::string& filename);

    // method for moving files from staging dir to target dir
    std::optional<std::string> moveFile(const std::filesystem::path& staging_path, const std::string& target_folder, const std::string& filename);

    public:
   FileProcessor(Database& database, 
                  const std::string& phys_base = "/data/seharacloud/media", 
                  const std::string& log_base = "/mnt/cloud");
    
    // main entry point which inofity calls when file appears in staging/
    bool process(const std::filesystem::path& filepath);
};

#endif