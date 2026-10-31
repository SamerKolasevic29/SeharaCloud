namespace SeharaCloud.Services;

using SeharaCloud.DTOs;
using SeharaCloud.Exceptions;
using SeharaCloud.Repositories.Interfaces;
using SeharaCloud.Services.Interfaces;

public class VideoService : IVideoService
{
    private readonly IVideoRepository _repo;
    private const int RecentLimit = 20;

    public VideoService(IVideoRepository repo) { _repo = repo;}

    public Task<IEnumerable<VideoDto>> GetAllAsync()
        => _repo.GetAllAsync();

    public Task<IEnumerable<VideoDto>> GetRecentAsync()
        => _repo.GetRecentAsync(RecentLimit);

    public async Task<IEnumerable<VideoDto>> SearchAsync(string query)
    {
        if (string.IsNullOrWhiteSpace(query) || query.Trim().Length < 2)
            throw new ValidationException("Search query must have length grater than 2");

        return await _repo.SearchVideoAsync(query.Trim());

    }

    public async Task<(string Path, string MimeType)> GetStreamInfoAsync(Guid id)
    {
        var info = await _repo.GetStreamInfoAsync(id);

        if (info is null)
            throw new NotFoundException($"Video with ID = {id} does not exist!");

        if (!File.Exists(info.Value.Path))
            throw new NotFoundException($"Multimedia file is missing on disk: {info.Value.Path}");

        return info.Value;
    }
}