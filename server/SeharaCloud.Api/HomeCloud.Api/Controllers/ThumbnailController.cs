namespace HomeCloud.Controllers;

using HomeCloud.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class ThumbnailController : ControllerBase
{
    private readonly IThumbnailService _service;

    public ThumbnailController(IThumbnailService service)
    {
        _service = service;
    }

    // GET /api/thumbnail/8867a4e3-dc6b-4081-8e71-7b320ad41105
    [HttpGet("{id:guid}")]
    public async Task<IActionResult> GetThumbnail(Guid id)
    {
        var path = await _service.GetThumbnailPathAsync(id);

        if (path is null)
            return NotFound();

        // choose mime via extension
        var mime = Path.GetExtension(path).ToLower() switch
        {
            ".jpg"  => "image/jpeg",
            ".jpeg" => "image/jpeg",
            ".png"  => "image/png",
            ".webp" => "image/webp",
            _       => "image/jpeg"
        };

        return PhysicalFile(path, mime);
    }
}