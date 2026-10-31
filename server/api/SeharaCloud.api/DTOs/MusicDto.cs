namespace SeharaCloud.DTOs;

public record MusicDto
{
    public Guid Id {get; init;}    
    public string Filename {get; init;} = "";
    public string? ThumbnailUrl {get; init;} //api for thumbnail stream
    public long SizeBytes { get; init; }

    public string? Title {get; init;} 
    public string? Artist {get; init;} // artist name
    public string? Genre {get; init;}
    public int? DurationSec {get; init;}
}