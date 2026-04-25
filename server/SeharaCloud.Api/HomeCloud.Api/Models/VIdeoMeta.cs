namespace HomeCloud.Models;
public class VideoMeta
{
   public Guid FileId { get; set; }
    public string? Title { get; set; }
    public string? Category { get; set; }
    public int? Year { get; set; }
    public int? DurationSec { get; set; }
    public string? Resolution { get; set; }
    public string? Codec { get; set; }
}