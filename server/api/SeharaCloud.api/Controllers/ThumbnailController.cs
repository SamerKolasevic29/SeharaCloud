namespace SeharaCloud.Controllers;

using SeharaCloud.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class ThumbnailController : ControllerBase
{
    private readonly IThumbnailService _service;

    public ThumbnailController(IThumbnailService service) {_service = service;}

    // GET /api/music/3fa85f64-5717-4562-b3fc-2c963f66afa6/stream
    [HttpGet("{id:guid}")]
    public async Task<IActionResult> Stream(Guid id)
    {
        var info = await _service.GetThumbnailInfoAsync(id);

        return PhysicalFile(
            physicalPath: info.Path,
            contentType: info.MimeType,
            enableRangeProcessing: true
        );
    }

}