namespace SeharaCloud.Repositories.Interfaces;

using SeharaCloud.DTOs;

public interface IVideoRepository
{
    Task<IEnumerable<VideoDto>> GetAllAsync();

    Task<IEnumerable<VideoDto>> GetRecentAsync(int limit);

    Task<IEnumerable<VideoDto>> SearchVideoAsync(string query);

     Task<(string Path, string MimeType)?> GetStreamInfoAsync(Guid id);

}