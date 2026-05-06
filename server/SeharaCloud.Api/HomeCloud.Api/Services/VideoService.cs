namespace HomeCloud.Services;

using HomeCloud.DTOs;
using HomeCloud.Enums;
using HomeCloud.Exceptions;
using HomeCloud.Repositories.Interfaces;
using HomeCloud.Services.Interfaces;

public class VideoService : IVideoService
{
    private readonly IVideoRepository _repo;
    private const int RecentLimit = 20;

    public VideoService(IVideoRepository repo)
    {
        _repo = repo;
    }

    public Task<IEnumerable<VideoDto>> GetAllAsync()
        => _repo.GetAllAsync();

    public Task<IEnumerable<VideoDto>> GetRecentAsync()
        => _repo.GetRecentAsync(RecentLimit);

    // these 3 methods exists in service but not in repo
    // Service knows what is "movie" || "documentary" || "other" (closed scope via enum!!)
    public Task<IEnumerable<VideoDto>> GetMoviesAsync()
        => _repo.GetByCategoryAsync(VideoCategory.movie);

    public Task<IEnumerable<VideoDto>> GetDocumentariesAsync()
        => _repo.GetByCategoryAsync(VideoCategory.documentary);

    public Task<IEnumerable<VideoDto>> GetOtherAsync()
        => _repo.GetByCategoryAsync(VideoCategory.other);

    public async Task<IEnumerable<VideoDto>> SearchAsync(string query)
    {
        if (string.IsNullOrWhiteSpace(query) || query.Trim().Length < 2)
            throw new ValidationException("Search query must have length grater than 2");

        return await _repo.SearchAsync(query.Trim());
    }

    public async Task<IEnumerable<VideoDto>> SearchByCategoryAsync(
        string query, VideoCategory category)
    {
        if (string.IsNullOrWhiteSpace(query) || query.Trim().Length < 2)
            throw new ValidationException("Search query must have length grater than 2");

        return await _repo.SearchByCategoryAsync(query.Trim(), category);
    }
}