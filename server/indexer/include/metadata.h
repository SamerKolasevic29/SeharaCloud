#ifndef METADATA_H
#define METADATA_H

#include <string>
#include <optional>
#include "db.h"

namespace Metadata {
// defining file type via extension
// returns "music" | "video" | "image" | "document" | "" (unknown)
std::string getFileType(const std::string& path);

// MIME type via extension
std::string getMimeType(const std::string& path);

// SHA256 hashing - for duplicate detection
std::string computeHash(const std::string& path);

// metadata extraction via type
// every func returns std::optional for safer handling fails
std::optional<MusicData>    extractMusic(const std::string& path);
std::optional<VideoData>    extractVideo(const std::string& path);
std::optional<ImageData>    extractImage(const std::string& path);
std::optional<DocumentData> extractDocument(const std::string& path);

// Thumbnail generation - we will se the usage of this beacuse of inconsistent metdata files...
// if doenst work properly - (inserting artists by hand, inserting thumbnails, handling via filename)

// for Video: ffmpeg extracts frame
// for MP3: cover art from ID3 tag
// for photo: resize original photo
// for PDF: poppler renders first page
std::string generateThumbnail(const std::string& filePath, const std::string& fileId, const std::string& fileType);


// MAIN FUNCTION - create complete fileRecord (calls all other funcs);
FileRecord buildRecord(const std::string& path); 

}

// metadata.h
#endif
