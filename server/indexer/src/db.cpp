#include "../include/db.h"
#include <iostream>
#include <stdexcept>

// Constructor - opens connection to PostgreSQL
// pqxx::connection handles connection while object lives

Database::Database(const std::string& connString) 
    : _conn(connString) 
    {
        if(_conn.is_open()) 
            std::cout << "[DB] Connection established: " << _conn.dbname() << "\n";
        
        else 
            throw std::runtime_error("[DB] Can't connect to Database!");
    }

// Checking duplicates via file_hash, stronger than checking via path
// because the duplicate can be with different path (or location, logically)

bool Database::fileExistsByHash(const std::string& hash) {
    if(hash.empty()) return false;

    pqxx::work txn(_conn);
    auto result = txn.exec_params(
        "SELECT 1 FROM files WHERE file_hash = $1 LIMIT 1",
         hash
    );

    txn.commit();
    return !result.empty();
}

bool Database::fileExistsByPath(const std::string& path) {
    if(path.empty()) return false;

    pqxx::work txn(_conn);
    auto result = txn.exec_params(
        "SELECT 1 FROM files HERE path = $1 LIMIT 1",
        path
    );

    txn.commit();
    return !result.empty();

}

//insertFle - main method
//Everything is running in transaction - something breaks, rollback
void Database::insertFile(const FileRecord& record) {
    
    //check duplicates via func-entering value's params (record)
    if(!record.fileHash.empty() && fileExistsByHash(record.fileHash)) {
        std::cout << "[DB] Skip the duplicate: " << record.filename << "\n";
        
        return;
    }

    if(fileExistsByPath(record.path)) {
        std::cout << "[DB] Already exists: " << record.filename << "\n";
        
        return;
    }

    try {
        pqxx::work txn(_conn); 
        
        //INESRT in 'files' table, RETURNING id gives the genrated UUID
        auto result = txn.exec_params(R"(
            INSERT INTO FILES
                (path, filename, file_type, mime_type, size_bytes, thumbnail_path, file_hash)
                VALUES ($1, $2, $3, $4, $5, $6, $7)
                RETURNING id::text
            )",

            record.path,
            record.filename,
            record.fileType,
            record.mimeType.empty()       ? std::nullopt : std::optional<std::string>(record.mimeType),
            record.sizeBytes,
            record.thumbnailPath.empty()  ? std::nullopt : std::optional<std::string>(record.thumbnailPath),
            record.fileHash.empty()       ? std::nullopt : std::optional<std::string>(record.fileHash)                                   
        );

        if(result.empty()) {
            txn.abort();
            std::cerr << "[DB] INSERT didn't return UUID for: " << record.filename << "\n";
            return;
        }

        std::string fileId = result[0][0].as<std::string>();

        // write in right meta table 
        if(record.music)        insertMusicMeta(txn, fileId, *record.music);
        if(record.video)        insertVideoMeta(txn, fileId, *record.video);
        if(record.image)        insertImageMeta(txn, fileId, *record.image);
        if(record. document)    insertDocumentMeta(txn, fileId, *record.document);
        
        txn.commit();
        std::cout << "[DB] Written:" << record.filename << " [" << fileId << "] \n";
    }
    catch (const std::exception& e) {
        std::cerr << "[DB] Insert error " << record.filename << ": " << e.what() << "\n";
    }

}

// deleteFile - calling this method when inotify sees physical delete
// uses ON DELETE CASCADE

void Database::deleteFile(const std::string& path) {
    
    try {
        pqxx::work txn(_conn);
        txn.exec_params("DELETE FROM files WHERE path = $1", path);
        txn.commit();
        std::cout << "[DB] Deleted: " << path << "\n";
    }

    catch (const std::exception& e) {
        std::cerr << "[DB] Delete error: " << e.what() << "\n";
    }
}

// inertOrGetArtist - UPSERT-like mehtod (insert if not exists) 
std::string Database::insertOrGetArtist(pqxx::work& txn, const std::string& name) {
    
    if(name.empty()) return "";

    txn.exec_params("INSERT INTO artists (name) VALUES ($1) ON CONFLICT (name) DO NOTHING", name);
    auto result = txn.exec_params("SELECT id::text FROM artists WHERE name = $1", name);

    return result.empty() ? "" : result[0][0].as<std::string>();
}

// inertOrGetGenre - UPSERT-like mehtod (insert if not exists) 
std::string Database::insertOrGetGenre(pqxx::work& txn, const std::string& name) {
    
    if(name.empty()) return "";

    txn.exec_params("INSERT INTO genres (name) VALUES ($1) ON CONFLICT (name) DO NOTHING", name);
    auto result = txn.exec_params("SELECT id::text FROM genres WHERE name = $1", name);

    return result.empty() ? "" : result[0][0].as<std::string>();
}


// Meta tables - calling them in insertFile when we have fileId
void Database::insertMusicMeta(pqxx::work& txn, const std::string& fileId, const MusicData& d) {

    std::string artistId = insertOrGetArtist(txn, d.artistName);
    std::string genreId = insertOrGetGenre(txn, d.genreName);

    // nullptr for value 0 beacuse 0 years isnt valid
    txn.exec_params(R"(
        INSERT INTO music_meta
            (file_id, artist_id, genre_id, album, year, duration_sec, bitrate, track_no)
        VALUES ($1, $2, $3::uuid, $4::uuid, $5, $6, $7, $8, $9)
        )", 
        
                fileId,
        d.title.empty() ? "Unknown" : d.title,
        artistId.empty() ? std::nullopt : std::optional<std::string>(artistId),
        genreId.empty()  ? std::nullopt : std::optional<std::string>(genreId),
        d.album.empty()  ? std::nullopt : std::optional<std::string>(d.album),
        d.year      == 0 ? std::nullopt : std::optional<int>(d.year),
        d.durationSec == 0 ? std::nullopt : std::optional<int>(d.durationSec),
        d.bitrate   == 0 ? std::nullopt : std::optional<int>(d.bitrate),
        d.trackNo   == 0 ? std::nullopt : std::optional<int>(d.trackNo)
    );

        // no .commit() beacuse these next 4 funcs are in mid process calls
}

void Database::insertVideoMeta(pqxx::work& txn,const std::string& fileId, const VideoData& d) {
    txn.exec_params(R"(
            INSERT INTO video_meta(file_id, title, category, year, duration_sec, resolution, codec)
            VALUES ($1, $2, $3, $4, $5, $6, $7)
        
        )",
            fileId,
            d.title.empty()      ? std::nullopt : std::optional<std::string>(d.title),
            d.category,
            d.year == 0          ? std::nullopt : std::optional<int>(d.year),
            d.durationSec == 0   ? std::nullopt : std::optional<int>(d.durationSec),
            d.resolution.empty() ? std::nullopt : std::optional<std::string>(d.resolution),
            d.codec.empty()      ? std::nullopt : std::optional<std::string>(d.codec)
    );
}

void Database::insertImageMeta(pqxx::work& txn, const std::string& fileId, const ImageData& d) {
    txn.exec_params(R"(
            INSERT INTO video_meta (file_id, width, height, date_taken, camera)
            VALUES ($1, $2, $3, $4, $5)
        )",

        fileId,
        d.width == 0 ? std::nullopt : std::optional<int>(d.width),
        d.height == 0 ? std::nullopt : std::optional<int>(d.height),
        d.dateTaken.empty() ? std::nullopt : std::optional<std::string>(d.dateTaken),
        d.camera.empty() ? std::nullopt : std::optional<std::string>(d.camera)
    
    );
}

void Database::insertDocumentMeta(pqxx::work& txn, const std::string& fileId, const DocumentData& d) {
    txn.exec_params(R"(
        INSERT INTO document_meta
            (file_id, title, category, author, page_count)
        VALUES ($1, $2, $3, $4, $5)
    )",
        fileId,
        d.title.empty()  ? std::nullopt : std::optional<std::string>(d.title),
        d.category,
        d.author.empty() ? std::nullopt : std::optional<std::string>(d.author),
        d.pageCount == 0 ? std::nullopt : std::optional<int>(d.pageCount)
    );
}