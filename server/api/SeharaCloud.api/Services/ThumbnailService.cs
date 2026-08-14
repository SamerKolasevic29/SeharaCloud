namespace SeharaCloud.Services;

using SeharaCloud.Exceptions;
using SeharaCloud.Repositories.Interfaces;
using SeharaCloud.Services.Interfaces;

public class ThumbnailService : IThumbnailService
{
    private readonly IThumbnailRepository _repo;

    public ThumbnailService(IThumbnailRepository repo)
    {
        _repo = repo;
    }

    public async Task<(string Path, string MimeType)> GetThumbnailInfoAsync(Guid thumbnailId)
    {
        var info = await _repo.GetThumbnailInfoAsync(thumbnailId);

        if (info is null)
            throw new NotFoundException($"Thumbnail za fajl {thumbnailId} ne postoji");

        if (!File.Exists(info.Value.Path))
            throw new NotFoundException($"Thumbnail fajl nije pronađen na disku: {info.Value.Path}");

        return info.Value;
    }
}