namespace HomeCloud.Services.Interfaces;

using HomeCloud.DTOs;
using HomeCloud.Enums;

public interface IVideoService
{
    Task<IEnumerable<VideoDto>> GetAllAsync();
    Task<IEnumerable<VideoDto>> GetRecentAsync();
    Task<IEnumerable<VideoDto>> GetMoviesAsync();
    Task<IEnumerable<VideoDto>> GetDocumentariesAsync();
    Task<IEnumerable<VideoDto>> GetOtherAsync();
    Task<IEnumerable<VideoDto>> SearchAsync(string query);
    Task<IEnumerable<VideoDto>> SearchByCategoryAsync(string query, VideoCategory category);
}