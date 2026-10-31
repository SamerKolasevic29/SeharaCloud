#include "../include/fileProcessor.h"
#include <iostream>
#include <fstream>
#include <iomanip>
#include <algorithm>
#include <sstream>
#include <memory>
#include <array>

// Libs for data manipulation 
#include <openssl/evp.h>
#include <taglib/fileref.h>
#include <taglib/audioproperties.h>
#include <poppler/cpp/poppler-document.h>
#include <Magick++.h>

namespace fs = std::filesystem;

FileProcessor::FileProcessor(Database& database, const std::string& phys_base, const std::string& log_base) 
    : db(database), physical_base(phys_base), logical_base(log_base) {
    
    // Initialization Magisk
    Magick::InitializeMagick(nullptr);
}

// ---------------------------------------------------------
// Helper for defining logical and physical path 
// ---------------------------------------------------------
FileProcessor::PathResult FileProcessor::preparePaths(const std::string& subfolder, const std::string& filename) {
    fs::path phys_dir = fs::path(physical_base) / subfolder;
    if (!fs::exists(phys_dir)) {
        fs::create_directories(phys_dir);
    }

    return {
        (phys_dir / filename).string(),            // Phisical path (/data/seharacloud/media/...)
        (fs::path(logical_base) / subfolder / filename).string() // Logical path (/mnt/cloud/...)
    };
}

// ---------------------------------------------------------
// Main Router for picked extensions
// ---------------------------------------------------------
bool FileProcessor::process(const fs::path& filepath) {
    if (!fs::exists(filepath)) return false;

    std::string filename = filepath.filename().string();
    std::string ext = filepath.extension().string();
    std::transform(ext.begin(), ext.end(), ext.begin(), ::tolower);

    if (ext == ".mp3" || ext == ".wav") {
        return processMusic(filepath, filename);
    } 
    else if (ext == ".mp4") {
        return processVideo(filepath, filename);
    }
    else if (ext == ".pdf" || ext == ".epub") {
        return processDocument(filepath, filename);
    }
    else if (ext == ".jpg" || ext == ".jpeg" || ext == ".png") {
        return processImage(filepath, filename);
    }
    
    std::cerr << "[PROCESSOR] Denied (Unsupported format): " << filename << std::endl;
    return false;
}

// ---------------------------------------------------------
// 1. Music
// ---------------------------------------------------------
bool FileProcessor::processMusic(const fs::path& staging_path, const std::string& filename) {
    MusicMeta meta;
    meta.filename = filename;
    meta.mime_type = determineMimeType(staging_path.extension().string());
    meta.bytes = fs::file_size(staging_path);
    meta.file_hash = calculateSHA256(staging_path);

    if (db.isDuplicate(meta.file_hash)) {
        std::cerr << "[PROCESSOR] Duplikat muzike! Brijsem iz staginga: " << filename << std::endl;
        fs::remove(staging_path);
        return false;
    }

    // Parsing filename: "Artist - songName.mp3"
    std::string stem = staging_path.stem().string();
    size_t dash_pos = stem.find(" - ");
    if (dash_pos != std::string::npos) {
        meta.artist_name = stem.substr(0, dash_pos);
        meta.title = stem.substr(dash_pos + 3);
    } else {
        meta.artist_name = "Unknown Artist";
        meta.title = stem;
    }

    // TagLib metadata
    TagLib::FileRef f(staging_path.c_str());
    if (!f.isNull() && f.audioProperties()) {
        meta.duration_sec = f.audioProperties()->length();
        meta.bitrate = f.audioProperties()->bitrate();
    } else {
        meta.duration_sec = 0; meta.bitrate = 0;
    }

    // Moving file to its dir and giving logical path for DB   
    auto paths = preparePaths("music", filename);
    fs::rename(staging_path, paths.physical_target);
    meta.path = paths.logical_target;

    return db.insertMusic(meta);
}

// ---------------------------------------------------------
// 2. Video (ffprobe)
// ---------------------------------------------------------
bool FileProcessor::processVideo(const fs::path& staging_path, const std::string& filename) {
    VideoMeta meta;
    meta.filename = filename;
    meta.title = staging_path.stem().string();
    meta.mime_type = "video/mp4";
    meta.bytes = fs::file_size(staging_path);
    meta.file_hash = calculateSHA256(staging_path);

    if (db.isDuplicate(meta.file_hash)) {
        fs::remove(staging_path);
        return false;
    }

    // Extracting metadata via ffprobe
    std::string cmd = "ffprobe -v error -select_streams v:0 "
                      "-show_entries stream=width,height,codec_name,duration "
                      "-of csv=p=0 \"" + staging_path.string() + "\"";
    std::string output = execCommand(cmd);
    
    // ffprobe returns format: codec,width,height,duration (exmple: h264,1920,1080,120.45)
    std::stringstream ss(output);
    std::string codec, w_str, h_str, dur_str;
    std::getline(ss, codec, ',');
    std::getline(ss, w_str, ',');
    std::getline(ss, h_str, ',');
    std::getline(ss, dur_str, ',');

    meta.codec = codec.empty() ? "unknown" : codec;
    meta.resolution = (!w_str.empty() && !h_str.empty()) ? (w_str + "x" + h_str) : "0x0";
    meta.duration_sec = dur_str.empty() ? 0 : static_cast<int>(std::stof(dur_str));

    auto paths = preparePaths("videos", filename);
    fs::rename(staging_path, paths.physical_target);
    meta.path = paths.logical_target;

    return db.insertVideo(meta);
}

// ---------------------------------------------------------
// 3. Documents (Poppler za PDF)
// ---------------------------------------------------------
bool FileProcessor::processDocument(const fs::path& staging_path, const std::string& filename) {
    DocumentMeta meta;
    meta.filename = filename;
    meta.title = staging_path.stem().string();
    meta.mime_type = determineMimeType(staging_path.extension().string());
    meta.bytes = fs::file_size(staging_path);
    meta.file_hash = calculateSHA256(staging_path);

    if (db.isDuplicate(meta.file_hash)) {
        fs::remove(staging_path);
        return false;
    }

    // Extracting page count
    meta.page_count = 0;
    if (staging_path.extension() == ".pdf") {
        auto doc = poppler::document::load_from_file(staging_path.string());
        if (doc) {
            meta.page_count = doc->pages();
            delete doc;
        }
    }

    auto paths = preparePaths("documents", filename);
    fs::rename(staging_path, paths.physical_target);
    meta.path = paths.logical_target;

    return db.insertDocument(meta);
}

// ---------------------------------------------------------
// 4. Images (Magick++ for dimensions and WebP thumbnail)
// ---------------------------------------------------------
bool FileProcessor::processImage(const fs::path& staging_path, const std::string& filename) {
    ImageMeta meta;
    meta.filename = filename;
    meta.title = staging_path.stem().string();
    meta.mime_type = determineMimeType(staging_path.extension().string());
    meta.bytes = fs::file_size(staging_path);
    meta.file_hash = calculateSHA256(staging_path);

    if (db.isDuplicate(meta.file_hash)) {
        fs::remove(staging_path);
        return false;
    }

    std::optional<ThumbnailMeta> thumb_meta = std::nullopt;

    try {
        Magick::Image img(staging_path.string());
        meta.width = img.columns();
        meta.height = img.rows();

        // Generation of .webp thumbnail
        std::string thumb_filename = meta.title + "_thumb.webp";
        auto thumb_paths = preparePaths("thumbnails", thumb_filename);

        Magick::Image thumb_img = img;
        thumb_img.resize(Magick::Geometry(512, 512)); // Max dimensions 512x512
        thumb_img.magick("WEBP");
        thumb_img.write(thumb_paths.physical_target); // Physical writing on disk

        thumb_meta = ThumbnailMeta{
            thumb_paths.logical_target, // Logical path for thumb
            static_cast<int>(thumb_img.columns()),
            static_cast<int>(thumb_img.rows())
        };

    } catch (const std::exception& e) {
        std::cerr << "[PROCESSOR] Magick++ error on image (" << filename << "): " << e.what() << std::endl;
    }

    auto paths = preparePaths("images", filename);
    fs::rename(staging_path, paths.physical_target);
    meta.path = paths.logical_target;

    return db.insertImage(meta, thumb_meta);
}

// ---------------------------------------------------------
// Technical methods (SHA256, execCommand, MimeType)
// ---------------------------------------------------------
std::string FileProcessor::calculateSHA256(const fs::path& filepath) {
    EVP_MD_CTX* context = EVP_MD_CTX_new();
    EVP_DigestInit_ex(context, EVP_sha256(), nullptr);

    std::ifstream file(filepath, std::ios::binary);
    char buffer[32768];
    while (file.read(buffer, sizeof(buffer))) {
        EVP_DigestUpdate(context, buffer, file.gcount());
    }
    EVP_DigestUpdate(context, buffer, file.gcount());

    unsigned char hash[EVP_MAX_MD_SIZE];
    unsigned int lengthOfHash = 0;
    EVP_DigestFinal_ex(context, hash, &lengthOfHash);
    EVP_MD_CTX_free(context);

    std::stringstream ss;
    for (unsigned int i = 0; i < lengthOfHash; ++i) {
        ss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];
    }
    return ss.str();
}

std::string FileProcessor::execCommand(const std::string& cmd) {
    std::array<char, 128> buffer;
    std::string result;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(cmd.c_str(), "r"), pclose);
    if (!pipe) return "";
    while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
        result += buffer.data();
    }
    return result;
}

std::string FileProcessor::determineMimeType(const std::string& extension) {
    std::string ext = extension;
    std::transform(ext.begin(), ext.end(), ext.begin(), ::tolower);

    if (ext == ".mp3") return "audio/mpeg";
    if (ext == ".wav") return "audio/wav";
    if (ext == ".mp4") return "video/mp4";
    if (ext == ".pdf") return "application/pdf";
    if (ext == ".epub") return "application/epub+zip";
    if (ext == ".jpg" || ext == ".jpeg") return "image/jpeg";
    if (ext == ".png") return "image/png";
    if (ext == ".webp") return "image/webp";

    return "application/octet-stream";
}