namespace SeharaCloud.Services.Interfaces;

public interface IThumbnailService
{
    Task<(string Path, string MimeType)?> GetThumbnailInfoAsync(Guid thumbnailId);
}