namespace HomeCloud.Services;

using HomeCloud.DTOs;
using HomeCloud.Exceptions;
using HomeCloud.Repositories.Interfaces;
using HomeCloud.Services.Interfaces;

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
}