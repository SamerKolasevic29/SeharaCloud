namespace HomeCloud.DTOs;

public record GenreDto
{
    public Guid Id { get; set; }
    public string Name { get; set; } = "";
    public int SongCount { get; set; }
    public string? ThumbnailUrl { get; set; }
}