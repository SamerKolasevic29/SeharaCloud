namespace HomeCloud.Services;

using HomeCloud.Exceptions;
using HomeCloud.Repositories.Interfaces;
using HomeCloud.Services.Interfaces;

public class ThumbnailService : IThumbnailService
{
    private readonly IThumbnailRepository _repo;

    public ThumbnailService(IThumbnailRepository repo)
    {
        _repo = repo;
    }

    public async Task<string?> GetThumbnailPathAsync(Guid fileId)
    {
        var path = await _repo.GetThumbnailPathAsync(fileId);

        if (path is null)
            throw new NotFoundException($"Thumbnail za fajl {fileId} ne postoji");

        if (!File.Exists(path))
            throw new NotFoundException($"Thumbnail fajl nije pronađen na disku: {path}");

        return path;
    }
}