## Music API
### GET /api/music
Returns all music files, sorted alphabetically.

### GET /api/music/recent
Returns the last 20 recently added songs.

### GET /api/music/artists
Returns a list of all artists along with their song counts.

### GET /api/music/artists/{artist}
Returns all songs belonging to a specific artist.

### GET /api/music/genres
Returns a list of all music genres.

### GET /api/music/genres/{genre}
Returns all songs belonging to a specific genre.

### GET /api/music/search?q={term}
Searches for music files matching the query term.

---

## Video API
### GET /api/videos
Returns a list of all video files.

### GET /api/videos/recent
Returns the last 20 recently added videos.

### GET /api/videos/search?q={term}
General search for any video file.

---

##Photos API
### GET /api/photos
Returns all images, sorted by date (newest first).

### GET /api/photos/{id}
Returns data for a single specific image.

---

##  Documents API
### GET /api/documents
Returns a list of all documents.

### GET /api/documents/search?q={term}
General search for any document.

---

## Core Media Services
### GET /api/stream/{id}
Streams media files with support for HTTP Range Requests (buffering/seeking).

### GET /api/thumbnails/{id}
Serves the thumbnail image for a specific file ID.

---