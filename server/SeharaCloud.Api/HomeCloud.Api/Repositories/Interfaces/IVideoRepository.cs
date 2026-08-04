namespace HomeCloud.Repositories.Interfaces;

using HomeCloud.DTOs;
using HomeCloud.Enums;

public interface IVideoRepository
{
    // -- Basic lists ------------------------
    Task<IEnumerable<VideoDto>> GetAllAsync();
    Task<IEnumerable<VideoDto>> GetRecentAsync(int limit);

    // -- Via Category ------------------------
    Task<IEnumerable<VideoDto>> GetByCategoryAsync(VideoCategory category);
    
    // -- Search ------------------------
    Task<IEnumerable<VideoDto>> SearchAsync(string query);
    Task<IEnumerable<VideoDto>> SearchByCategoryAsync(string query, VideoCategory category);
}