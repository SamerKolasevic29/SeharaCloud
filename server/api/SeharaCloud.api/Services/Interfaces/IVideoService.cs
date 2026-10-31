namespace SeharaCloud.Services.Interfaces;

using SeharaCloud.DTOs;

public interface IVideoService
{
    Task<IEnumerable<VideoDto>> GetAllAsync();

    Task<IEnumerable<VideoDto>> GetRecentAsync();

    Task<IEnumerable<VideoDto>> SearchAsync(string query);
    Task<(string Path, string MimeType)> GetStreamInfoAsync(Guid id);
}