#ifndef DB_H
#define DB_H

#include <string>
#include <optional>
#include <pqxx/pqxx>

// defining structs via DB - every meta table and files table
// in FileRecord structs each *file*Meta will be _optional_

struct MusicData {
    std::string title;
    std::string artistName; // db will find/create UUID, in this place, artistName is okay
    std::string genreName; //as same as artistName philosophy
    std::string album;
    int year        = 0;
    int durationSec = 0;
    int bitrate     = 0;
    int trackNo     = 0;
    std::string thumbnailPath; // cover art if exists
};

struct VideoData {
    std::string title;
    std::string category = "other"; // movie | documentary | other
    int year            = 0;
    int durationSec     = 0;
    std::string resolution;
    std::string codec;
};

struct ImageData {
    int width   = 0;
    int height  = 0;
    std::string dateTaken;  // ISO format: "2024-03-15 14:30:00+01"
    std::string camera;
};

struct DocumentData {
    std::string title;
    std::string category = "document"; // book || document || other
    std::string author;
    int pageCount = 0;
};

struct FileRecord {
    std::string path;
    std::string filename;
    std::string fileType; // video | music | image | document
    std::string mimeType;
    long long   sizeBytes = 0;
    std::string thumbnailPath;
    std::string fileHash; // SHA256

    // only one of these four will be not std::nullopt;
    std::optional<MusicData>    music;
    std::optional<VideoData>    video;
    std::optional<ImageData>    image;
    std::optional<DocumentData> document;
};

// Database - encapsulates all SQL operations
// One object lives whole time in program

class Database {
public:

    explicit Database(const std::string& connstring);
    ~Database() = default;

    // Check if file already exists in DB via hash
    bool fileExistsByHash(const std::string& hash);

    // Check if file already exists in DB via path 
    bool fileExistsByPath(const std::string& path);

    // Insert new file - internaly calls insertMusic/Video/Image/Document 
    void insertFile(const FileRecord& record);

    // Delete file from DB when inotify see that
    void deleteFile(const std::string& path);


private:
    pqxx::connection _conn;

    //Helper methods - insertFile calls them
    std::string insertOrGetArtist(pqxx::work& txn, const std::string& name);
    std::string insertOrGetGenre (pqxx::work& txn, const std::string& name);

    void insertMusicMeta   (pqxx::work& txn, const std::string& fileId, const MusicData& d);
    void insertVideoMeta   (pqxx::work& txn, const std::string& fileId, const VideoData& d);
    void insertImageMeta   (pqxx::work& txn, const std::string& fileId, const ImageData& d);
    void insertDocumentMeta(pqxx::work& txn, const std::string& fileId, const DocumentData& d);

};

//db.h
#endif