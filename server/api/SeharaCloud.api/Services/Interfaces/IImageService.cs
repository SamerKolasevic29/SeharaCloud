namespace SeharaCloud.Services.Interfaces;

using SeharaCloud.DTOs;

public interface IImageService
{
    Task<IEnumerable<ImageDto>> GetAllAsync();
    Task<ImageDto?> GetByIdAsync(Guid id);
    Task<(string Path, string MimeType)> GetStreamInfoAsync(Guid id);
}