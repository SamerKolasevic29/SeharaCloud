namespace SeharaCloud.DTOs;

public record GenreDto
{
    public Guid Id {get; init;}
    public string Name {get; init;} = "";
    public int SongCount {get; init;}
    public string? ThumbnailUrl {get; init;}
}