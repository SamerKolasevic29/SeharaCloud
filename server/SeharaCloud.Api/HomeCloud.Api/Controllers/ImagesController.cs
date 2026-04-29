// Controllers/PhotosController.cs
namespace HomeCloud.Controllers;

using HomeCloud.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class ImagesController : ControllerBase
{
    private readonly IImageService _service;

    public ImagesController(IImageService service)
    {
        _service = service;
    }

    // GET /api/images
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var result = await _service.GetAllAsync();
        return Ok(result);
    }

    // GET /api/images/3fa85f64-5717-4562-b3fc-2c963f66afa6
    [HttpGet("{id:guid}")]
    public async Task<IActionResult> GetById(Guid id)
    {
        var result = await _service.GetByIdAsync(id);
        return Ok(result);
    }
}