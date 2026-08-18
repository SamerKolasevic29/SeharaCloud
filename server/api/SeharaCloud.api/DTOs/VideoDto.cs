namespace SeharaCloud.DTOs;

public record VideoDto
{
    public Guid Id {get; init;}
    public string Filename {get; init;} = "";
    public string? ThumbnailUrl {get; init;}
    public long SizeBytes { get; init; }
    public string? Title {get; init;}
    public int? DurationSec{get; init;}
    public string? Resolution {get; init;}
    public string? Codec {get; init;}
}