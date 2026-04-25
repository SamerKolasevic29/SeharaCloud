namespace HomeCloud.DTOs;

public record ArtistDto
{
    public Guid Id { get; set; }
    public string Artist { get; set; } = "";
    public int SongCount { get; set; }
    public string? ThumbnailUrl { get; set; }
}
