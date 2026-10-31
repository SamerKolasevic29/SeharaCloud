namespace SeharaCloud.Repositories.Interfaces;

using SeharaCloud.DTOs;

public interface IImageRepository
{
    Task<IEnumerable<ImageDto>> GetAllAsync();
    Task<ImageDto?> GetByIdAsync(Guid id);
    Task<(string Path, string MimeType)?> GetStreamInfoAsync(Guid id);

}