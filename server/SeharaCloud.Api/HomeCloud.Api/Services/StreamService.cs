namespace HomeCloud.Services;

using HomeCloud.Exceptions;
using HomeCloud.Repositories.Interfaces;
using HomeCloud.Services.Interfaces;

public class StreamService : IStreamService
{
    private readonly IStreamRepository _repo;

    public StreamService(IStreamRepository repo)
    {
        _repo = repo;
    }

    public async Task<(string Path, string MimeType)?> GetStreamInfoAsync(Guid fileId)
    {
        var info = await _repo.GetFileInfoAsync(fileId);

        if (info is null)
            throw new NotFoundException($"File with ID = {fileId} does not exits!");

        // Check does that file exists on that file path
        if (!File.Exists(info.Value.Path))
            throw new NotFoundException($"File is not found on the disk: {info.Value.Path}");

        return info;
    }
}