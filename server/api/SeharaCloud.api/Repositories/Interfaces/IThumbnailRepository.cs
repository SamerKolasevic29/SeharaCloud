namespace SeharaCloud.Repositories.Interfaces;

public interface IThumbnailRepository
{
    Task<string?> GetThumbnailPathAsync(Guid id);
}