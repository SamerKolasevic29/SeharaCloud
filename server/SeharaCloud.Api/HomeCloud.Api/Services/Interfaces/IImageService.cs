namespace HomeCloud.Services.Interfaces;

using HomeCloud.DTOs;

public interface IImageService
{
    Task<IEnumerable<ImageDto>> GetAllAsync();
    Task<ImageDto?> GetByIdAsync(Guid id);
}