namespace SeharaCloud.Repositories.Interfaces;

using SeharaCloud.DTOs;

public interface IThumbnailRepository
{
    Task<string?> GetThumbnailPathAsync(Guid id);
}