#include "../include/db.h"
#include <iostream>
#include <random>

// Contructor

Database::Database(const std::string& conn_str) : conn(conn_str) {
    if(conn.is_open()) 
        std::cout << "[DB] Succesful connection: " << conn.dbname() << std::endl;
    
    else
        std::cerr << "[DB] ERROR: Impossible to open a connection!" << std::endl;  
}

Database::~Database() {
    if(conn.is_open())
        conn.close();
}

// helper for finding duplicates
bool Database::isDuplicate(const std::string& file_hash) {
    try {
        pqxx::work W(conn);

        // DISCLAIMER: we will see the performances when DB kept growing (reason is UNION ALL)
        pqxx::result R = W.exec_params("SELECT id FROM fn_find_duplicate($1) LIMIT 1", file_hash);
        return !R.empty();
    }
    catch (const std::exception& e) {
        std::cerr << "[DB] ERROR while checking duplicates: " << e.what() << std::endl;
        return false;
    }

}

// InsertMusic
bool Database::insertMusic(const MusicMeta& meta) {
    try {
        pqxx::work W(conn);
        pqxx::result artist_res = W.exec_params(
            "SELECT id, thumbnail_id2, thumbnail_id3 FROM fn_resolve_artist($1)", meta.artist_name);

        if(artist_res.empty()) return false;
    
        std::string artist_id = artist_res[0]["id"].c_str();
        std::optional<std::string> thumb2;
        std::optional<std::string> thumb3;
        
        if (!artist_res[0]["thumbnail_id2"].is_null()) thumb2 = artist_res[0]["thumbnail_id2"].c_str();
        if (!artist_res[0]["thumbnail_id3"].is_null()) thumb3 = artist_res[0]["thumbnail_id3"].c_str();

        std::string final_thumbnail_id = "00000000-0000-0000-0000-000000000014";

        if (thumb2.has_value() && thumb3.has_value()) {
            // We have both of them, flip a coin
            std::random_device rd;
            std::mt19937 gen(rd());
            std::uniform_int_distribution<> distrib(0, 1);
            final_thumbnail_id = distrib(gen) == 0 ? thumb2.value() : thumb3.value();
        }

        else if (thumb2.has_value()) { final_thumbnail_id = thumb2.value(); }
        else if (thumb3.has_value()) { final_thumbnail_id = thumb3.value(); }

        // insert in base
        W.exec_params(
            "INSERT INTO music (path, filename, mime_type, file_hash, size_bytes, title, artist_id, duration_sec, bitrate, thumbnail_id) "
            "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)",
            meta.path, meta.filename, meta.mime_type, meta.file_hash, meta.bytes, meta.title, artist_id, meta.duration_sec, meta.bitrate, final_thumbnail_id
        );

        W.commit(); // transaction succesful
        return true;
    }

    catch (const std::exception& e) {
        std::cerr << "[DB] ERROR while inserting music (" << meta.filename << "): " << e.what() << std::endl;
        return false;
    }
}

// video insert
bool Database::insertVideo(const VideoMeta& meta) {
    try {
        pqxx::work W(conn);
        // Thumbnail is automaticaly solved
        W.exec_params(
            "INSERT INTO videos (path, filename, mime_type, file_hash, size_bytes, title, duration_sec, resolution, codec) "
            "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)",
            meta.path, meta.filename, meta.mime_type, meta.file_hash, meta.bytes, meta.title, meta.duration_sec, meta.resolution, meta.codec
        );
        W.commit();
        return true;
    } catch (const std::exception& e) {
        std::cerr << "[DB] ERROR while inserting video: " << e.what() << std::endl;
        return false;
    }
}

// document insert
bool Database::insertDocument(const DocumentMeta& meta) {
    try {
        pqxx::work W(conn);
        // Thumbnail is automaticaly solved
        W.exec_params(
            "INSERT INTO documents (path, filename, mime_type, file_hash, size_bytes, title, page_count) "
            "VALUES ($1, $2, $3, $4, $5, $6, $7)",
            meta.path, meta.filename, meta.mime_type, meta.file_hash, meta.bytes, meta.title, meta.page_count
        );
        W.commit();
        return true;
    } catch (const std::exception& e) {
        std::cerr << "[DB] ERROR while inserting document: " << e.what() << std::endl;
        return false;
    }
}

// image insert
bool Database::insertImage(const ImageMeta& meta, const std::optional<ThumbnailMeta>& thumbnail) {
    try {
        pqxx::work W(conn);
        std::string final_thumb_id = "00000000-0000-0000-0000-000000000017"; // Default image thumb[cite: 7]

        // If we have a generated webp,we must firstly insert thumbnail then an image
        if (thumbnail.has_value()) {
            pqxx::result R = W.exec_params(
                "INSERT INTO thumbnails (path, width, height) VALUES ($1, $2, $3) RETURNING id",
                thumbnail->path, thumbnail->width, thumbnail->height
            );
            final_thumb_id = R[0][0].c_str(); // generated UUID of thumbnail
        }

        // Insertion of an image with correct or default thumbnail
        // Handling NULL values
        auto date_taken_param = meta.date_taken.has_value() ? std::optional<std::string>(meta.date_taken.value()) : std::nullopt;
        auto camera_param = meta.camera.has_value() ? std::optional<std::string>(meta.camera.value()) : std::nullopt;

        W.exec_params(
            "INSERT INTO images (path, filename, mime_type, file_hash, size_bytes, title, width, height, date_taken, camera, thumbnail_id) "
            "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)",
            meta.path, meta.filename, meta.mime_type, meta.file_hash, meta.bytes, meta.title, 
            meta.width, meta.height, date_taken_param, camera_param, final_thumb_id
        );

        W.commit();
        return true;
    } catch (const std::exception& e) {
        std::cerr << "[DB] ERROR while inserting an image: " << e.what() << std::endl;
        return false;
    }
}