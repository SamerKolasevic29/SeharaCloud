namespace SeharaCloud.Controllers;

using SeharaCloud.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class VideosController : ControllerBase
{
    private readonly IVideoService _service;

    public VideosController(IVideoService service) { _service = service; }

     // GET /api/videos
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var result = await _service.GetAllAsync();
        return Ok(result);
    }

     // GET /api/videos/recent
    [HttpGet("recent")]
    public async Task<IActionResult> GetRecent()
    {
        var result = await _service.GetRecentAsync();
        return Ok(result);
    }

     // GET /api/videos/search?q=something
    [HttpGet("search")]
    public async Task<IActionResult> Search([FromQuery] string q)
    {
        var result = await _service.SearchAsync(q);
        return Ok(result);
    }

    // GET /api/videos/3fa85f64-5717-4562-b3fc-2c963f66afa6/stream
    [HttpGet("{id:guid}/stream")]
    public async Task<IActionResult> Stream(Guid id)
    {
        var info = await _service.GetStreamInfoAsync(id);

        return PhysicalFile(
            physicalPath: info.Path,
            contentType: info.MimeType,
            enableRangeProcessing: true
        );
    }
}