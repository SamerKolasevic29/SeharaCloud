namespace HomeCloud.DTOs;

public record GenreDto
{
    public Guid Id { get; set; }
    public string Genre { get; set; } = "";
    public int SongCount { get; set; }
    public string? ThumbnailUrl { get; set; }
}