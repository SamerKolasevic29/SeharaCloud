namespace HomeCloud.DTOs;

public record VideoDto
{
    public Guid Id { get; set; }
    public string Filename { get; set; } = "";
    public string? ThumbnailUrl { get; set; }
     public string? Title { get; set; } 
    public string? Category { get; set; }
    public int? Year { get; set; }
    public int? DurationSec { get; set; }
   public string? Resolution { get; set; }
    public string? Codec { get; set; }
}