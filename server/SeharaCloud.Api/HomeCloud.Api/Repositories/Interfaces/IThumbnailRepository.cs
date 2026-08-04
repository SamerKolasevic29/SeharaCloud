namespace HomeCloud.Repositories.Interfaces;

public interface IThumbnailRepository
{
    Task<string?> GetThumbnailPathAsync(Guid fileId);
}