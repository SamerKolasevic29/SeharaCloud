namespace HomeCloud.Models;
public class Artist
{
    public Guid Id { get; set; }
    public string Name { get; set; } = "";
    public string? ThumbnailUrl { get; set; }
    public string? Bio { get; set; }
}