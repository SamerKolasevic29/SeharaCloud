namespace HomeCloud.DTOs;
public record DocumentDto
{
    public Guid Id { get; set; }
    public string? Title { get; set; }
    public string? Category { get; set; }
    public string? Author { get; set; }
    public int PageCount { get; set; }
    public string? ThumbnailPath { get; set; }
    public long SizeBytes { get; set; }
}