namespace HomeCloud.Repositories.Interfaces;

public interface IStreamRepository
{
    // Returns physical path and MIMe type for that file_id
    Task<(string Path, string MimeType)?> GetFileInfoAsync(Guid fileId);
}