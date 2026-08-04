using HomeCloud.Middleware;
using HomeCloud.Repositories;
using HomeCloud.Repositories.Interfaces;
using HomeCloud.Services;
using HomeCloud.Services.Interfaces;
using Npgsql;

var builder = WebApplication.CreateBuilder(args);

// ── DB ───────────────────────────────────────────
var connectionString = builder.Configuration
    .GetConnectionString("DefaultConnection")!;

builder.Services.AddNpgsqlDataSource(connectionString);

// ── Repozitories ───────────────────────────────────
builder.Services.AddScoped<IMusicRepository, MusicRepository>();
builder.Services.AddScoped<IVideoRepository, VideoRepository>();
builder.Services.AddScoped<IDocumentRepository, DocumentRepository>();
builder.Services.AddScoped<IImageRepository, ImageRepository>();
builder.Services.AddScoped<IStreamRepository, StreamRepository>();
builder.Services.AddScoped<IThumbnailRepository, ThumbnailRepository>();

// ── Services ────────────────────────────────────────
builder.Services.AddScoped<IMusicService, MusicService>();
builder.Services.AddScoped<IVideoService, VideoService>();
builder.Services.AddScoped<IDocumentService, DocumentService>();
builder.Services.AddScoped<IImageService, ImageService>();
builder.Services.AddScoped<IStreamService, StreamService>();
builder.Services.AddScoped<IThumbnailService, ThumbnailService>();

// ── API ────────────────────────────────────────────
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// ── Middleware pipeline ────────────────────────────
app.UseMiddleware<ExceptionMiddleware>();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.MapControllers();

app.Run();