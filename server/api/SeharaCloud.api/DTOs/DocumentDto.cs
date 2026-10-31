namespace SeharaCloud.DTOs;

public record DocumentDto
{
    public Guid Id {get; init;}
    public string Filename {get; init;} = "";
    public string? ThumbnailUrl {get; init;}
    public long SizeBytes { get; init; }
    public string? Title {get; init;}
    public int? PageCount {get; init;}
}