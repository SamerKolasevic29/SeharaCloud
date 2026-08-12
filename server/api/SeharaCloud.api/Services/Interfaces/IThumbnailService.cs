namespace SeharaCloud.Services.Interfaces;

public interface IThumbnailService
{
    Task<string?> GetThumbnailPathAsync(Guid fileId);
}