namespace HomeCloud.Models;
public class ImageMeta
{
    public Guid FileId { get; set; }
    public int? Width { get; set; }
    public int? Height { get; set; }
    public DateTimeOffset? DateTaken { get; set; }
    public string? Camera { get; set; }
}