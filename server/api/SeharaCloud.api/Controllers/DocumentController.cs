namespace SeharaCloud.Controllers;

using SeharaCloud.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class DocumentController : ControllerBase
{
    private readonly IDocumentService _service;

    public DocumentController(IDocumentService service) {_service = service;}

    // GET /api/documents
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var result = await _service.GetAllAsync();
        return Ok(result);
    }

    // GET /api/documents/recent
    [HttpGet]
    public async Task<IActionResult> GetRecentAsync()
    {
        var result = await _service.GetRecentAsync();
        return Ok(result);
    }

    // GET /api/documents/search?q=Linux
    [HttpGet]
    public async Task<IActionResult> Search([FromQuery] string q)
    {
        var result = await _service.SearchDocumentAsync(q);
        return Ok(result);
    }

   // GET /api/documents/3fa85f64-5717-4562-b3fc-2c963f66afa6/stream
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