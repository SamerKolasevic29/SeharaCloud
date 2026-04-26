namespace HomeCloud.Services.Interfaces;

using HomeCloud.DTOs;

public interface IMusicService
{
    Task<IEnumerable<MusicDto>> GetAllAsync();
    Task<IEnumerable<MusicDto>> GetRecentAsync();
    Task<IEnumerable<ArtistDto>> GetArtistsAsync();
    Task<IEnumerable<MusicDto>> GetByArtistIdAsync(Guid artistId);
    Task<IEnumerable<GenreDto>> GetGenresAsync();
    Task<IEnumerable<MusicDto>> GetByGenreIdAsync(Guid genreId);
    Task<IEnumerable<MusicDto>> SearchAsync(string query);
    Task<IEnumerable<ArtistDto>> SearchArtistsAsync(string query);
}
