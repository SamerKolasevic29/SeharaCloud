namespace HomeCloud.Services.Interfaces;

public interface IStreamService
{
    // returns (path, mimeType) tuple or null if not exists
    Task<(string Path, string MimeType)?> GetStreamInfoAsync(Guid fileId);
}