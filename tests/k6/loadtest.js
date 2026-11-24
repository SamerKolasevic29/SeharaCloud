import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:5000';

export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
  stages: [
    { duration: '25s', target: 10 }, // Warm-up (.NET JIT + Npgsql pool)
    { duration: '50s', target: 25 }, // Normalno opterećenje (baseline)
    { duration: '50s', target: 60 }, // Stress pik (p99 tačka pucanja)
    { duration: '15s', target: 0 },  // Cooldown
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

function safeJson(res) {
  if (res.status !== 200) return [];
  try {
    const body = res.json();
    return Array.isArray(body) ? body : [];
  } catch (_) {
    return [];
  }
}

function extractId(item) {
  return item.id || item.Id || null;
}

function extractThumbnailPath(item) {
  const url = item.thumbnailUrl || item.ThumbnailUrl || '';
  const match = url.match(/(\/api\/thumbnail\/[^/?#]+)/i);
  return match ? match[1] : null;
}

function extractSearchTerm(items, fields) {
  for (const item of items) {
    for (const f of fields) {
      const val = (item[f] || '').toString().trim();
      if (val.length >= 3) {
        return encodeURIComponent(val.substring(0, 4));
      }
    }
  }
  return 'aa';
}

export function setup() {
  const music = safeJson(http.get(`${BASE_URL}/api/music`));
  const artists = safeJson(http.get(`${BASE_URL}/api/music/artists`));
  const genres = safeJson(http.get(`${BASE_URL}/api/music/genres`));
  const videos = safeJson(http.get(`${BASE_URL}/api/videos`));
  const images = safeJson(http.get(`${BASE_URL}/api/images`));
  const documents = safeJson(http.get(`${BASE_URL}/api/documents`));

  const thumbPaths = new Set();
  [...music, ...artists, ...videos, ...images, ...documents].forEach((item) => {
    const p = extractThumbnailPath(item);
    if (p) thumbPaths.add(p);
  });

  const payload = {
    musicIds: music.map(extractId).filter(Boolean),
    artistIds: artists.map(extractId).filter(Boolean),
    genreIds: genres.map(extractId).filter(Boolean),
    videoIds: videos.map(extractId).filter(Boolean),
    imageIds: images.map(extractId).filter(Boolean),
    documentIds: documents.map(extractId).filter(Boolean),
    thumbnailPaths: Array.from(thumbPaths),
    musicQuery: extractSearchTerm(music, ['title', 'Title', 'filename', 'Filename']),
    artistQuery: extractSearchTerm(artists, ['name', 'Name']),
    videoQuery: extractSearchTerm(videos, ['title', 'Title', 'filename', 'Filename']),
    docQuery: extractSearchTerm(documents, ['title', 'Title', 'filename', 'Filename']),
  };

  console.log(
    `Učitano iz baze -> Muzika: ${payload.musicIds.length}, Izvođači: ${payload.artistIds.length}, ` +
    `Žanrovi: ${payload.genreIds.length}, Video: ${payload.videoIds.length}, ` +
    `Slike: ${payload.imageIds.length}, Dokumenti: ${payload.documentIds.length}, Thumbnails: ${payload.thumbnailPaths.length}`
  );

  return payload;
}

function pickRandom(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

export default function (data) {
  const roll = Math.random();

  if (roll < 0.40) {
    group('01_Listings', () => {
      const endpoints = [
        '/api/music',
        '/api/music/recent',
        '/api/music/artists',
        '/api/music/genres',
        '/api/videos',
        '/api/videos/recent',
        '/api/images',
        '/api/documents',
        '/api/documents/recent',
      ];
      const ep = pickRandom(endpoints);
      const res = http.get(`${BASE_URL}${ep}`, { tags: { endpoint: ep } });
      check(res, { 'listing 200 OK': (r) => r.status === 200 });
    });
  } else if (roll < 0.60) {
    group('02_Details', () => {
      const candidates = [];
      if (data.artistIds.length) candidates.push(`/api/music/artists/${pickRandom(data.artistIds)}`);
      if (data.genreIds.length) candidates.push(`/api/music/genres/${pickRandom(data.genreIds)}`);
      if (data.imageIds.length) candidates.push(`/api/images/${pickRandom(data.imageIds)}`);

      if (candidates.length) {
        const url = pickRandom(candidates);
        const res = http.get(`${BASE_URL}${url}`, { tags: { endpoint: 'detail_by_id' } });
        check(res, { 'detail 200 OK': (r) => r.status === 200 });
      }
    });
  } else if (roll < 0.80) {
    group('03_Search', () => {
      const searches = [
        `/api/music/search?q=${data.musicQuery}`,
        `/api/music/artists/search?q=${data.artistQuery}`,
        `/api/videos/search?q=${data.videoQuery}`,
        `/api/documents/search?q=${data.docQuery}`,
      ];
      const ep = pickRandom(searches);
      const res = http.get(`${BASE_URL}${ep}`, { tags: { endpoint: 'search' } });
      check(res, { 'search 200 OK': (r) => r.status === 200 });
    });
  } else {
    group('04_Media_And_Thumbnails', () => {
      if (data.thumbnailPaths.length && Math.random() < 0.5) {
        const thumbPath = pickRandom(data.thumbnailPaths);
        const res = http.get(`${BASE_URL}${thumbPath}`, { tags: { endpoint: 'thumbnail' } });
        check(res, { 'thumbnail 200 OK': (r) => r.status === 200 });
      } else {
        const streams = [];
        if (data.musicIds.length) streams.push(`/api/music/${pickRandom(data.musicIds)}/stream`);
        if (data.videoIds.length) streams.push(`/api/videos/${pickRandom(data.videoIds)}/stream`);
        if (data.documentIds.length) streams.push(`/api/documents/${pickRandom(data.documentIds)}/stream`);
        if (data.imageIds.length) streams.push(`/api/images/${pickRandom(data.imageIds)}/stream`);

        if (streams.length) {
          const streamUrl = pickRandom(streams);
          const res = http.get(`${BASE_URL}${streamUrl}`, {
            headers: { Range: 'bytes=0-65535' },
            tags: { endpoint: 'stream_range' },
          });
          check(res, { 'stream 200/206 OK': (r) => r.status === 206 || r.status === 200 });
        }
      }
    });
  }

  sleep(0.2);
}
