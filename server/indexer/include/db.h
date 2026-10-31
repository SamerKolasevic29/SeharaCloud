#ifndef DB_H
#define DB_H


#include <string>
#include <optional>
#include <pqxx/pqxx>

struct CommonMeta {
    std::string path;
    std::string filename;
    std::string mime_type;
    std::string file_hash;
    long long bytes;
    std::string title;
};

struct MusicMeta : CommonMeta {
    std::string artist_name;
    int duration_sec;
    int bitrate;
};

struct VideoMeta : CommonMeta {
    int duration_sec;
    std::string resolution;
    std::string codec;
};

struct DocumentMeta : CommonMeta {
    int page_count;
};

struct ImageMeta : CommonMeta {
    int width;
    int height;
    std::optional<std::string> date_taken; // std::optional because in DB date_taken can be NULL
    std::optional<std::string> camera;
};

struct ThumbnailMeta {
    std::string path;
    int width;
    int height;
};

class Database {
private:
    pqxx::connection conn; // libpqxx connection

public:
    // Contstructor
    explicit Database(const std::string& conn_str);

    // Destructor
    ~Database();

    // Main Methods for insertion
    bool insertMusic(const MusicMeta& meta);
    bool insertVideo(const VideoMeta& meta);
    bool insertDocument(const DocumentMeta& meta);
    
    // Method for images and generated thumbnail (if created)
    bool insertImage(const ImageMeta& meta, const std::optional<ThumbnailMeta>& thumbnail);

    // Helper method for checking duplicates with psql function fn_find_duplicate
    bool isDuplicate(const std::string& file_hash);
};

#endif