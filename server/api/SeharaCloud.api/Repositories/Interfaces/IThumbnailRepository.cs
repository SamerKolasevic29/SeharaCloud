namespace SeharaCloud.Repositories.Interfaces;

public interface IThumbnailRepository
{
    Task<(string Path, string MimeType)?> GetThumbnailInfoAsync(Guid id);
}