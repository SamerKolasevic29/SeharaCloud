namespace SeharaCloud.Services;

using SeharaCloud.DTOs;
using SeharaCloud.Exceptions;
using SeharaCloud.Repositories.Interfaces;
using SeharaCloud.Services.Interfaces;

public class ImageService : IImageService
{
    private readonly IImageRepository _repo;

    public ImageService(IImageRepository repo)
    {
        _repo = repo;
    }

    public Task<IEnumerable<ImageDto>> GetAllAsync()
        => _repo.GetAllAsync();

    public async Task<ImageDto?> GetByIdAsync(Guid id)
    {
        var image = await _repo.GetByIdAsync(id);

        if (image is null)
            throw new NotFoundException($"Image with ID = {id} does not exist!");

        return image;
    }

    public async Task<(string Path, string MimeType)> GetStreamInfoAsync(Guid id)
    {
        var info = await _repo.GetStreamInfoAsync(id);

        if (info is null)
            throw new NotFoundException($"Photo with ID = {id} does not exist!");

        if (!File.Exists(info.Value.Path))
            throw new NotFoundException($"Image file is missing on disk: {info.Value.Path}");

        return info.Value;
    }
}