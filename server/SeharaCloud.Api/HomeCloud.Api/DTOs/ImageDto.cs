namespace HomeCloud.DTOs;

public record ImageDto
{
    public Guid Id { get; set; }
    public string FileName { get; set; } = "";
    public string? ThumbnailUrl { get; set; } = "";
    public int? Width { get; set; }
    public int? Height { get; set; }
    public DateTimeOffset? DateTaken { get; set; }
    public string? Camera { get; set; } 
    public long SizeBytes { get; set; }
}