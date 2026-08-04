namespace HomeCloud.Controllers;

using HomeCloud.Services.Interfaces;
using Microsoft.AspNetCore.Identity.Data;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class StreamController : ControllerBase
{
    private readonly IStreamService _service;

    public StreamController(IStreamService service) { _service = service; }

    // GET /api/stream/3fa85f64-5717-4562-b3fc-2c963f66afa6
    public async Task<IActionResult> Stream(Guid fileId)
    {
        var info = await _service.GetStreamInfoAsync(fileId);

        if(info is null) 
            return NotFound();

        // PhysicalFile serves file from disk
        // enableRangeProcessing: true _ ASP.NET automatically processes
        // Range header and returns 206 Partial Content
        return PhysicalFile (
            physicalPath: info.Value.Path,
            contentType: info.Value.MimeType,
            enableRangeProcessing: true
        );
    }
}