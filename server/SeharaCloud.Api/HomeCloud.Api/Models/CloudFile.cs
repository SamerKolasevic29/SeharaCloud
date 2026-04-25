namespace HomeCloud.Models;

    public class CloudFile
    {
        public Guid Id {get; set;}
        public string Path {get; set;} = "";
        public string FileName {get; set;} = "";
        public string FileType { get; set; } = "";
        public string? MimeType { get; set; }
        public long SizeBytes { get; set; }
        public string? ThumbnailPath { get; set; }

        public DateTimeOffset IndexedAt { get; set; }
        public string? FileHash { get; set; }
    }