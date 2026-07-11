#include "../include/metadata.h"
#include <filesystem>
#include <fstream>
#include <sstream>
#include <iomanip>
#include <iostream>
#include <algorithm>
#include <cstdio>

// TagLib - MP3/FLAC/OGG metadata
#include <taglib/fileref.h>
#include <taglib/tag.h>
#include <taglib/audioproperties.h>
#include <taglib/mpegfile.h>
#include <taglib/id3v2tag.h>
#include <taglib/attachedpictureframe.h>

//libexiv2 - EXIF from photos
#include <exiv2/exif.hpp>
#include <exiv2/image.hpp>

// poppler - PDF
#include <poppler/cpp/poppler-document.h>

//OpenSSL - SHA256
#include <openssl/sha.h>

namespace fs = std::filesystem;

// getFileType - via extension sets type
// returns empty string for unknown types

std::string Metadata::getFileType(const std::string& path) {

    std::string ext = fs::path(path).extension().string();

    // lowercase for better comparison
    std::transform(ext.begin(), ext.end(), ext.begin(), ::towlower);

    if(ext == ".mp3" || ext == ".flac" || ext == ".ogg" ||
       ext == ".m4a" || ext == ".wav")

        return "music";

    if (ext == ".mp4" || ext == ".mkv" || ext == ".avi" ||
        ext == ".mov" || ext == ".webm")

        return "video";

    if (ext == ".jpg" || ext == ".jpeg" || ext == ".png" ||
        ext == ".gif" || ext == ".webp")

        return "image";

    if(ext == ".pdf")
        return "document";

    return "";

}

// getMimeType - for HTTP header 

std::string Metadata::getMimeType(const std::string& path) {
    
    std::string ext = fs::path(path).extension().string();
    std::transform(ext.begin(), ext.end(), ext.begin(), ::towlower);

    if (ext == ".mp3")  return "audio/mpeg";
    if (ext == ".flac") return "audio/flac";
    if (ext == ".ogg")  return "audio/ogg";
    if (ext == ".m4a")  return "audio/mp4";
    if (ext == ".wav")  return "audio/wav";
    if (ext == ".mp4")  return "video/mp4";
    if (ext == ".mkv")  return "video/x-matroska";
    if (ext == ".avi")  return "video/x-msvideo";
    if (ext == ".mov")  return "video/quicktime";
    if (ext == ".webm") return "video/webm";
    if (ext == ".jpg" || ext == ".jpeg") return "image/jpeg";
    if (ext == ".png")  return "image/png";
    if (ext == ".webp") return "image/webp";
    if (ext == ".gif")  return "image/gif";
    if (ext == ".pdf")  return "application/pdf";
 
    return "application/octet-stream";
}



// computeHash - 3 phases (init, update, final)
//  reading 8kb of file and hash that 

std::string Metadata::computeHash(const std::string& path) {

    // "rb" means read binary 
    FILE* f = fopen(path.c_str(), "rb");

    if(!f) return "";

    SHA256_CTX ctx;
    SHA256_Init(&ctx);

    unsigned char buf[8192];
    size_t bytes;
    while((bytes = fread(buf, 1,sizeof(buf), f)) != 0)
        SHA256_Update(&ctx, buf, bytes);

    fclose(f);

    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256_Final(hash, &ctx);

    std::stringstream ss;
    for(int i = 0; i < SHA256_DIGEST_LENGTH; i++)
        ss << std::hex << std::setw(2) << std::setfill('0') << (int)hash[i];

    return ss.str();
}

// extractMusic - TagLib reads ID3 Tags
std::optional<MusicData> Metadata::extractMusic(const std::string& path) {

    MusicData data;

    try {
        TagLib::FileRef f(path.c_str());

        if(f.isNull()) {
            std::cerr << "[Metadata] Taglib vannot open: " << path << "\n";
            return std::nullopt;
        }

        if(f.tag()) {
            data.title      = f.tag()->title().to8Bit(true);
            data.artistName = f.tag()->artist().to8Bit(true);
            data.album      = f.tag()->album().to8Bit(true);
            data.genreName  = f.tag()->genre().to8Bit(true);
            data.year       = (int)f.tag()->year();
            data.trackNo    = (int)f.tag()->track();
        }

        if(f.audioProperties()) {
            data.durationSec = f.audioProperties()->lengthInSeconds();
            data.bitrate     = f.audioProperties()->bitrate();
        }

        // fallback for empty fields
        if (data.title.empty())      data.title      = "Unknown Title";
        if (data.artistName.empty()) data.artistName = "Unknown";
        if (data.genreName.empty())  data.genreName  = "";
    }

    catch(const std::exception& e) {
        std::cerr << "[Metadata] MP3 read error: " << e.what() << "\n";
        return std::nullopt;
    }

    return data;
}


// extractVideo - ffprobe CLI for metadata
std::optional<VideoData> Metadata::extractVideo(const std::string& path) {
    VideoData data;
    data.category = "other";

    // duration 
    std::string cmd =
    "ffprobe -v quiet -print_format csv=p=0 "
    "-show_entries format=duration "
    "\"" + path + "\" 2>dev/null";

    FILE* pipe = popen(cmd.c_str(), "r");
    if(pipe) {
        char buf[128];

        if(fgets(buf, sizeof(buf), pipe))
            data.durationSec = (int)atof(buf);
        pclose(pipe);
    }

    //resolution + codec
    std::string cmd2 =
    "ffprobe -v quiet -print_format csv=p=0 "
    "-show_entries stream=codec_name,width,height "
    "-select_streams v:0 "
    "\"" + path + "\" 2>/dev/null";

    FILE* pipe2 = popen(cmd2.c_str(), "r");
    if(pipe2) {
        char buf2[256];

        if(fgets(buf2, sizeof(buf2), pipe2)) {
            std::string line(buf2);
            std::istringstream iss(line);
            std::string codec, wStr, hStr;
            std::getline(iss, codec, ',');
            std::getline(iss, wStr, ',');
            std::getline(iss, hStr, ',');

            data.codec = codec;
            if(!wStr.empty() && !hStr.empty())
                data.resolution = wStr + "x" + hStr;
        }
        pclose(pipe2);
    }

        return data;
}

// extractImage - libexiv2 reads EXIF
std::optional<ImageData> Metadata::extractImage(const std::string& path) {
    ImageData data;

    try {
        auto image = Exiv2::ImageFactory::open(path);
        image->readMetadata();
        auto& exif = image->exifData();

        auto widthIt = exif.findKey(Exiv2::ExifKey("Exif.Photo.PixelXDimension"));
        if (widthIt != exif.end())
            data.width = (int)widthIt->value().toUint32();

        auto heightIt = exif.findKey(Exiv2::ExifKey("Exif.Photo.PixelYDimension"));
        if (heightIt != exif.end())
            data.height = (int)heightIt->value().toUint32();

        auto dateIt = exif.findKey(Exiv2::ExifKey("Exif.Photo.DateTimeOriginal"));
        if (dateIt != exif.end()) {
            std::string raw = dateIt->value().toString();
            // "2024:03:15 14:30:00" → "2024-03-15 14:30:00"
            if (raw.size() >= 10) {
                raw[4] = '-';
                raw[7] = '-';
                data.dateTaken = raw;
            }
        }

        auto makeIt  = exif.findKey(Exiv2::ExifKey("Exif.Image.Make"));
        auto modelIt = exif.findKey(Exiv2::ExifKey("Exif.Image.Model"));

        if (makeIt != exif.end())
            data.camera = makeIt->value().toString();
        if (modelIt != exif.end()) {
            if (!data.camera.empty()) data.camera += " ";
            data.camera += modelIt->value().toString();
        }

    } catch (const Exiv2::Error&) {
        // nije greška — screenshot, generirane slike, itd.
        std::cout << "[Metadata] No EXIF: "
                  << fs::path(path).filename().string() << "\n";
    }

    return data;
}

// extractDocument - Popplerrads PDF metadata
std::optional<DocumentData> Metadata::extractDocument(const std::string& path) {
    DocumentData data;

    auto doc = poppler::document::load_from_file(path);
    if (!doc) {
        std::cerr << "[Metadata] Cannot open PDF: " << path << "\n";
        return std::nullopt;
    }

    data.pageCount = doc->pages();

    auto title = doc->get_title();
    if (!title.empty())
        data.title = title.to_latin1();

    auto author = doc->get_author();
    if (!author.empty())
        data.author = author.to_latin1();

    // pokušaj pogoditi kategoriju iz naziva
    std::string stem = fs::path(path).stem().string();
    std::transform(stem.begin(), stem.end(), stem.begin(), ::tolower);

    if (stem.find("book") != std::string::npos ||
        stem.find("knjiga") != std::string::npos)
        data.category = "book";
    else
        data.category = "document";

    return data;
}


// ─────────────────────────────────────────────────────────────
std::string Metadata::generateThumbnail(const std::string& filePath,
                                         const std::string& fileId,
                                         const std::string& fileType) {
    const std::string thumbDir  = "/data/seharacloud/media/thumbnails/";
    const std::string thumbPath = thumbDir + fileId + ".jpg";

    fs::create_directories(thumbDir);

    if (fileType == "video") {
        std::string cmd =
            "ffmpeg -ss 5 -i \"" + filePath + "\" "
            "-vframes 1 -vf scale=320:-1 "
            "-q:v 2 \"" + thumbPath + "\" -y -loglevel quiet";
        if (system(cmd.c_str()) != 0) return "";
        return thumbPath;
    }

    if (fileType == "music") {
        try {
            TagLib::MPEG::File mpegFile(filePath.c_str());
            auto* tag = mpegFile.ID3v2Tag();
            if (!tag) return "";

            auto frames = tag->frameListMap()["APIC"];
            if (frames.isEmpty()) return "";

            auto* pic = dynamic_cast<TagLib::ID3v2::AttachedPictureFrame*>(frames.front());
            if (!pic) return "";

            auto picData = pic->picture();
            std::ofstream out(thumbPath, std::ios::binary);
            out.write(picData.data(), picData.size());
            return thumbPath;

        } catch (...) {
            return "";
        }
    }

    if (fileType == "image") {
        std::string cmd =
            "ffmpeg -i \"" + filePath + "\" "
            "-vf scale=320:-1 "
            "-q:v 2 \"" + thumbPath + "\" -y -loglevel quiet";
        if (system(cmd.c_str()) != 0) return "";
        return thumbPath;
    }

    if (fileType == "document") {
        const std::string ppmBase = thumbDir + fileId;
        std::string cmd =
            "pdftoppm -jpeg -r 72 -f 1 -l 1 "
            "\"" + filePath + "\" \"" + ppmBase + "\" 2>/dev/null";

        system(cmd.c_str());

        std::string generated = ppmBase + "-1.jpg";
        if (fs::exists(generated)) {
            fs::rename(generated, thumbPath);
            return thumbPath;
        }
        return "";
    }

    return "";
}

// ─────────────────────────────────────────────────────────────
FileRecord Metadata::buildRecord(const std::string& path) {
    FileRecord rec;
    rec.path      = path;
    rec.filename  = fs::path(path).filename().string();
    rec.fileType  = getFileType(path);
    rec.mimeType  = getMimeType(path);
    rec.sizeBytes = (long long)fs::file_size(path);
    rec.fileHash  = computeHash(path);

    if      (rec.fileType == "music")    rec.music    = extractMusic(path);
    else if (rec.fileType == "video")    rec.video    = extractVideo(path);
    else if (rec.fileType == "image")    rec.image    = extractImage(path);
    else if (rec.fileType == "document") rec.document = extractDocument(path);

    return rec;
}




