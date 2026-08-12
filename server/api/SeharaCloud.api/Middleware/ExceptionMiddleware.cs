namespace SeharaCloud.Middleware; 

using System.Net;
using System.Text.Json;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Logging;
using SeharaCloud.Exceptions; 

public class ExceptionMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ExceptionMiddleware> _logger;

    
    // _next is next middleware in chain
    // _logger iz built-in .NET _logger
    public ExceptionMiddleware(RequestDelegate next, ILogger<ExceptionMiddleware> logger)
    {
        _next = next;
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            // foward request to the next middleware in chain
            await _next(context);
        }
        catch (NotFoundException ex) 
        {
            _logger.LogWarning("Not found: {Message}", ex.Message);
            await WriteErrorResponse(context, HttpStatusCode.NotFound, ex.Message);
        }
        catch (ValidationException ex) 
        {
            _logger.LogWarning("Validation error: {Message}", ex.Message);
            await WriteErrorResponse(context, HttpStatusCode.BadRequest, ex.Message);
        }
        catch (Exception ex)
        {
            //everything else is 500 - log complete stack trace
            _logger.LogError(ex, "Unhandled exception occurred.");
            await WriteErrorResponse(
                context, 
                HttpStatusCode.InternalServerError, 
                "Problem on server"
            );
        }
    }

    private static async Task WriteErrorResponse(
        HttpContext context, 
        HttpStatusCode statusCode, 
        string message)
    {
        context.Response.ContentType = "application/json";
        context.Response.StatusCode = (int)statusCode;

        var response = new
        {
            status = (int)statusCode,
            error = message
        };

        var json = JsonSerializer.Serialize(response, new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase
        });

        await context.Response.WriteAsync(json);
    }
}