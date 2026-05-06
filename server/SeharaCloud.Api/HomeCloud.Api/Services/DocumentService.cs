namespace HomeCloud.Services;

using HomeCloud.DTOs;
using HomeCloud.Enums;
using HomeCloud.Exceptions;
using HomeCloud.Repositories.Interfaces;
using HomeCloud.Services.Interfaces;

public class DocumentService : IDocumentService
{
    private readonly IDocumentRepository _repo;

    public DocumentService(IDocumentRepository repo)
    {
        _repo = repo;
    }

    public Task<IEnumerable<DocumentDto>> GetAllAsync()
        => _repo.GetAllAsync();

    // these 3 methods exists in service but not in repo
    // Service knows what is "book" || "document" || "other" (closed scope via enum!!)
    public Task<IEnumerable<DocumentDto>> GetBooksAsync()
        => _repo.GetByCategoryAsync(DocumentCategory.book);

    public Task<IEnumerable<DocumentDto>> GetDocumentsAsync()
        => _repo.GetByCategoryAsync(DocumentCategory.document);

    public Task<IEnumerable<DocumentDto>> GetOtherAsync()
        => _repo.GetByCategoryAsync(DocumentCategory.other);

    public async Task<IEnumerable<DocumentDto>> SearchAsync(string query)
    {
        if (string.IsNullOrWhiteSpace(query) || query.Trim().Length < 2)
            throw new ValidationException("Search query must have length greater than 2");

        return await _repo.SearchAsync(query.Trim());
    }

    public async Task<IEnumerable<DocumentDto>> SearchByCategoryAsync(
        string query, DocumentCategory category)
    {
        if (string.IsNullOrWhiteSpace(query) || query.Trim().Length < 2)
            throw new ValidationException("Search query must have length greater than 2");

        return await _repo.SearchByCategoryAsync(query.Trim(), category);
    }
}