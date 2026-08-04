namespace HomeCloud.Repositories.Interfaces;

using HomeCloud.DTOs;

public interface IImageRepository
{
    Task<IEnumerable<ImageDto>> GetAllAsync();
    Task<ImageDto?> GetByIdAsync(Guid id);
}