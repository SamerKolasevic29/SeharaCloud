// Controllers/DocsController.cs
namespace HomeCloud.Controllers;

using HomeCloud.Enums;
using HomeCloud.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class DocsController : ControllerBase
{
    private readonly IDocumentService _service;

    public DocsController(IDocumentService service)
    {
        _service = service;
    }

    // GET /api/docs
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var result = await _service.GetAllAsync();
        return Ok(result);
    }

    // GET /api/docs/books
    [HttpGet("books")]
    public async Task<IActionResult> GetBooks()
    {
        var result = await _service.GetBooksAsync();
        return Ok(result);
    }

    // GET /api/docs/books/search?q=linux
    [HttpGet("books/search")]
    public async Task<IActionResult> SearchBooks([FromQuery] string q)
    {
        var result = await _service.SearchByCategoryAsync(q, DocumentCategory.book);
        return Ok(result);
    }

    // GET /api/docs/documents
    [HttpGet("documents")]
    public async Task<IActionResult> GetDocuments()
    {
        var result = await _service.GetDocumentsAsync();
        return Ok(result);
    }

    // GET /api/docs/documents/search?q=something
    [HttpGet("documents/search")]
    public async Task<IActionResult> SearchDocuments([FromQuery] string q)
    {
        var result = await _service.SearchByCategoryAsync(q, DocumentCategory.document);
        return Ok(result);
    }

    // GET /api/docs/other
    [HttpGet("other")]
    public async Task<IActionResult> GetOther()
    {
        var result = await _service.GetOtherAsync();
        return Ok(result);
    }

    // GET /api/docs/other/search?q=something
    [HttpGet("other/search")]
    public async Task<IActionResult> SearchOther([FromQuery] string q)
    {
        var result = await _service.SearchByCategoryAsync(q, DocumentCategory.other);
        return Ok(result);
    }

    // GET /api/docs/search?q=something
    [HttpGet("search")]
    public async Task<IActionResult> Search([FromQuery] string q)
    {
        var result = await _service.SearchAsync(q);
        return Ok(result);
    }
}