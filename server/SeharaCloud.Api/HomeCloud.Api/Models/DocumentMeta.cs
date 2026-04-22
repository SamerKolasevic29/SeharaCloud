namespace HomeCloud.Models;

using HomeCloud.Enums;

public class DocumentMeta
{
    public Guid FileId { get; set; }
    public string? Title { get; set; }
    public string? Category { get; set; }
    public string? Author { get; set; }
    public int? PageCount { get; set; }
}