namespace SeharaCloud.Services;

using SeharaCloud.DTOs;
using SeharaCloud.Exceptions;
using SeharaCloud.Repositories.Interfaces;
using SeharaCloud.Services.Interfaces;

public class MusicService : IMusicService
{
    private readonly IMusicRepository _repo;
    private const int RecentLimit = 20;

    public MusicService(IMusicRepository repo) {_repo = repo;}

    public Task<IEnumerable<MusicDto>> GetAllAsync() => _repo.GetAllAsync();
    public Task<IEnumerable<MusicDto>> GetRecentAsync() => _repo.GetRecentAsync(RecentLimit);
    public Task<IEnumerable<ArtistDto>> GetArtistsAsync() => _repo.GetArtistsAsync();
    public async Task<IEnumerable<MusicDto>> GetByArtistIdAsync(Guid artistId)
    {
         var songs = await _repo.GetByArtistIdAsync(artistId);

        // if list is empty - artist exists but has no songs or artist himself does not exists
        // in that cases returns an empty list, not a 404, frontend will handle empty list

        return songs;
    }

    public Task<IEnumerable<GenreDto>> GetGenresAsync() => _repo.GetGenresAsync();
    public Task<IEnumerable<MusicDto>> GetByGenreIdAsync(Guid genreId) => _repo.GetByGenreIdAsync(genreId);
    public async Task<IEnumerable<MusicDto>> SearchAsync(string query)
    {
        // Validation - Service doesn't let empty query to go in communication w/ DB
        if (string.IsNullOrWhiteSpace(query))
            throw new ValidationException("Search query cannot be empty");

        // Minimal length is 2
        if (query.Trim().Length < 2)
            throw new ValidationException("Search query must have length grater than 2");

        return await _repo.SearchMusicAsync(query.Trim());
    }

    public async Task<IEnumerable<ArtistDto>> SearchArtistsAsync(string query)
    {
        if (string.IsNullOrWhiteSpace(query) || query.Trim().Length < 2)
            throw new ValidationException("Search query must have length grater than 2");

        return await _repo.SearchArtistAsync(query.Trim());
    }




}