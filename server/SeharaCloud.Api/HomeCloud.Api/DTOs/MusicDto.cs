namespace HomeCloud.DTOs;

public record MusicDto
{
    public Guid Id { get; set; }
    public string Filename { get; set; } = "";
    public string? ThumbnailUrl { get; set; }
    public string? Title { get; set; }
    public string? Artist { get; set; }
    public string? Album { get; set; }
    public string? Genre { get; set; }
    public int? DurationSec { get; set; }    
}