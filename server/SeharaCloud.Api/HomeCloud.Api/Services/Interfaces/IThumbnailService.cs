namespace HomeCloud.Services.Interfaces;

public interface IThumbnailService
{
     Task<string?> GetThumbnailPathAsync(Guid fileId);
}