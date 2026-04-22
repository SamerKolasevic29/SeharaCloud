namespace HomeCloud.Models;

public class MusicMeta
{
    public Guid FileId { get; set; }
    public string? Title { get; set; }
    public string? Artist { get; set; }    
    public string? Album { get; set; }
    public string? Genre { get; set; }
    public int? Year { get; set; }
    public int? DurationSec { get; set; }
    public int? Bitrate { get; set; }
    public int? TrackNO { get; set; }
}