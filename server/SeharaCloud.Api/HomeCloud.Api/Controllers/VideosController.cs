namespace HomeCloud.Controllers;

using HomeCloud.Enums;
using HomeCloud.Services.Interfaces;
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

    // GET /api/videos/movies
    [HttpGet("movies")]
    public async Task<IActionResult> GetMovies()
    {
        var result = _service.GetMoviesAsync();
        return Ok(result);
    }

    // GET /api/videos/movies/search?q=inception
    [HttpGet("movies/search")]
    public async Task<IActionResult> SearchMovies([FromQuery] string q)
    {
        var result = await _service.SearchByCategoryAsync(q, VideoCategory.movie);
        return Ok(result);
    }

     // GET /api/videos/documentaries
    [HttpGet("documentaries")]
    public async Task<IActionResult> GetDocumentaries()
    {
        var result = await _service.GetDocumentariesAsync();
        return Ok(result);
    }

    // GET /api/videos/documentaries/search?q=planet
    [HttpGet("documentaries/search")]
    public async Task<IActionResult> SearchDocumentaries([FromQuery] string q)
    {
        var result = await _service.SearchByCategoryAsync(q, VideoCategory.documentary);
        return Ok(result);
    }

    // GET /api/videos/other
    [HttpGet("other")]
    public async Task<IActionResult> GetOther()
    {
        var result = await _service.GetOtherAsync();
        return Ok(result);
    }

    // GET /api/videos/other/search?q=something
    [HttpGet("other/search")]
    public async Task<IActionResult> SearchOther([FromQuery] string q)
    {
        var result = await _service.SearchByCategoryAsync(q, VideoCategory.other);
        return Ok(result);
    }

    // GET /api/videos/search?q=something
    [HttpGet("search")]
    public async Task<IActionResult> Search([FromQuery] string q)
    {
        var result = await _service.SearchAsync(q);
        return Ok(result);
    }
}  