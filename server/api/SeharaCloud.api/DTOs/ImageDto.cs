namespace SeharaCloud.DTOs;

public record ImageDto
{
    public Guid Id {get; init;}
    public string Filename {get; init;} = "";
    public string? ThumbnailUrl {get; init;}
    public long SizeBytes { get; init; }
    public string? Title {get; init;}
    public int? Width {get; init;}
    public int? Height {get; set;}
    public DateTimeOffset? DateTaken {get; init;}
    public string? Camera {get; init;}
}