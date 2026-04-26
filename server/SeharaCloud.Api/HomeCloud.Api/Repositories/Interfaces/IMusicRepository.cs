namespace HomeCloud.Repositories.Interfaces;

using System.Drawing;
using HomeCloud.DTOs;

public interface IMusicRepository
{
    // -- Basic lists ------------------------
    Task<IEnumerable<MusicDto>> GetAllAsync();
    Task<IEnumerable<MusicDto>> GetRecentAsync(int limit);

    // -- Artist ------------------------
    Task<IEnumerable<ArtistDto>> GetArtistsAsync();
    Task<IEnumerable<MusicDto>> GetArtistByIdAsync(Guid artistId);

    // -- Genre ------------------------
    Task<IEnumerable<GenreDto>> GetGenresAsync();
    Task<IEnumerable<MusicDto>> GetGenreByIdAsync(Guid genreId);

    // -- Search ------------------------
    Task<IEnumerable<MusicDto>> SearchAsync(string query);
    Task<IEnumerable<ArtistDto>> SearchArtistsAsync(string query);
}