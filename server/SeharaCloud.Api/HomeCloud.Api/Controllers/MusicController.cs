namespace HomeCloud.Controllers;

using HomeCloud.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class MusicController : ControllerBase
{
    private readonly IMusicService _service;

    public MusicController(IMusicService service) { _service = service; }


    // GET /api/music
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var result = await _service.GetAllAsync();
        return Ok(result);
    }

    // GET /api/music/recent
    [HttpGet("recent")]
    public async Task<IActionResult> GetRecent()
    {
        var result = await _service.GetRecentAsync();
        return Ok(result);
    }

    // GET /api/music/artists
    [HttpGet("artists")]
    public async Task<IActionResult> GetArtists()
    {
        var result = await _service.GetArtistsAsync();
        return Ok(result);
    }

    // GET /api/music/artists/search?q=SmokeMardeljano
    [HttpGet("artists/search")]
    public async Task<IActionResult> SearchArtists([FromQuery] string q)
    {
        var result = await _service.SearchArtistsAsync(q);
        return Ok(result);
    }

    // GET /api/music/artists/3fa85f64-5717-4562-b3fc-2c963f66afa6
    [HttpGet("artists/{artistId:guid}")]
    public async Task<IActionResult> GetByArtist(Guid artistId)
    {
        var result = await _service.GetByArtistIdAsync(artistId);
        return Ok(result);
    }

    // GET /api/music/genres
    [HttpGet("genres")]
    public async Task<IActionResult> GetGenres()
    {
        var result = await _service.GetGenresAsync();
        return Ok(result);
    }

    // GET /api/music/genres/3fa85f64-5717-4562-b3fc-2c963f66afa6
    [HttpGet("genres/{genreId:guid}")]
    public async Task<IActionResult> GetByGenre(Guid genreId)
    {
        var result = await _service.GetByGenreIdAsync(genreId);
        return Ok(result);
    }

    // GET api/music/search?q=avko
    [HttpGet("search")]
    public async Task<IActionResult> Search([FromQuery] string q)
    {
        var result = await _service.SearchAsync(q);
        return Ok(result);
    }
}