using Npgsql;
using SeharaCloud.Middleware;
using SeharaCloud.Repositories;
using SeharaCloud.Repositories.Interfaces;
using SeharaCloud.Services;
using SeharaCloud.Services.Interfaces;

var builder = WebApplication.CreateBuilder(args);

// ============================================================
// 1. DATABASE CONNECTION (PostgreSQL / Npgsql)
// ============================================================
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection") 
    ?? throw new InvalidOperationException("Connection string 'DefaultConnection' is missing!");

// Register NpgsqlDataSource as Singleton (recommended & fastest approach in .NET)
builder.Services.AddNpgsqlDataSource(connectionString);

// ============================================================
// 2. REPOSITORY REGISTRATION (Dependency Injection)
// ============================================================
builder.Services.AddScoped<IMusicRepository, MusicRepository>();
builder.Services.AddScoped<IVideoRepository, VideoRepository>();
builder.Services.AddScoped<IImageRepository, ImageRepository>();
builder.Services.AddScoped<IDocumentRepository, DocumentRepository>();
builder.Services.AddScoped<IThumbnailRepository, ThumbnailRepository>();

// ============================================================
// 3. SERVICE REGISTRATION (Business Logic)
// ============================================================
builder.Services.AddScoped<IMusicService, MusicService>();
builder.Services.AddScoped<IVideoService, VideoService>();
builder.Services.AddScoped<IImageService, ImageService>();
builder.Services.AddScoped<IDocumentService, DocumentService>();
// builder.Services.AddScoped<IThumbnailService, ThumbnailService>(); // Uncomment if ThumbnailService is implemented

// ============================================================
// 4. CONTROLLERS & SWAGGER / API EXPLORER
// ============================================================
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// CORS Configuration (for React, Angular, or external web clients)
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

// ============================================================
// 5. MIDDLEWARE PIPELINE (Order of execution matters)
// ============================================================

//  STEP 0: Custom ExceptionMiddleware MUST be registered first!
// Intercepts all unhandled exceptions, NotFoundException, and ValidationException instances
app.UseMiddleware<ExceptionMiddleware>();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAll");

app.UseRouting();

app.UseAuthorization();

app.MapControllers();

app.Run();