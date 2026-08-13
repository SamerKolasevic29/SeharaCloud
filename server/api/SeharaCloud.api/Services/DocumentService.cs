namespace SeharaCloud.Services;

using SeharaCloud.DTOs;
using SeharaCloud.Exceptions;
using SeharaCloud.Repositories.Interfaces;
using SeharaCloud.Services.Interfaces;

public class DocumentService : IDocumentService
{
    private readonly IDocumentRepository _repo;
    private const int RecentLimit = 20;

    public DocumentService(IDocumentRepository repo) { _repo = repo;}

    public Task<IEnumerable<DocumentDto>> GetAllAsync() => _repo.GetAllAsync();

    public Task<IEnumerable<DocumentDto>> GetRecentAsync() => _repo.GetRecentAsync(RecentLimit);

    public async Task<IEnumerable<DocumentDto>> SearchDocumentAsync(string query)
    {
        // Validation - Service doesn't let empty query to go in communication w/ DB
        if (string.IsNullOrWhiteSpace(query))
            throw new ValidationException("Search query cannot be empty");

        // Minimal length is 2
        if (query.Trim().Length < 2)
            throw new ValidationException("Search query must have length grater than 2");

        return await _repo.SearchDocumentAsync(query.Trim());
    }
}