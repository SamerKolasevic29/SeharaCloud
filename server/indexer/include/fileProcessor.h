#ifndef FILEPROCESSOR_H
#define FILEPROCESSOR_H

#include <string>
#include <filesystem>
#include <db.h>

class FileProcessor {
private:
    Database& db;
    std::string base_path;  // example: /mnt/cloud

    // helper methods
    std::string calculateSHA256(const std::filesystem::path& filepath);
    std::string determineMimeType(const std::string& extension);

    // methods for metadata extraction for each file type
    bool proccesMusic(const std::filesystem::path& staging_path, const std::string& filename);    
    bool proccesVideo(const std::filesystem::path& staging_path, const std::string& filename);    
    bool proccesDocument(const std::filesystem::path& staging_path, const std::string& filename);
    bool proccesImage(const std::filesystem::path& staging_path, const std::string& filename);

    // method for moving files from staging dir to target dir
    std::optional<std::string> moveFile(const std::filesystem::path& staging_path, const std::string& target_folder, const std::string& filename);

    public:
    FileProcessor(Database& database, const std::string& base_dir);
    
    // main entry point which inofity calls when file appears in staging/
    bool process(const std::filesystem::path& filepath);
};

#endif