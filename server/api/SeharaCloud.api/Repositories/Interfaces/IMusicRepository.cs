namespace SeharaCloud.Repositories.Interfaces;

using SeharaCloud.DTOs;

public interface IMusicRepository
{
    // -- Basic lists ----------
    Task<IEnumerable<MusicDto>> GetAllAsync();
    Task<IEnumerable<MusicDto>> GetRecentAsync(int limit);

    // -- Artists ----------
    Task<IEnumerable<ArtistDto>> GetArtistsAsync();
    Task<IEnumerable<MusicDto>> GetByArtistIdAsync(Guid artistId);

    // -- Genres ----------
    Task<IEnumerable<GenreDto>> GetGenresAsync();
    Task<IEnumerable<MusicDto>> GetByGenreIdAsync(Guid genreId);

    // -- Search ----------
    Task<IEnumerable<MusicDto>> SearchMusicAsync(string query);
    Task<IEnumerable<ArtistDto>> SearchArtistAsync(string query);
}