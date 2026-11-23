# === STAGE 1: Build ===
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# 1. Kopiranje csproj fajla u tačan direktorijum i restore paketa
COPY server/api/SeharaCloud.api/*.csproj ./server/api/SeharaCloud.api/
RUN dotnet restore ./server/api/SeharaCloud.api/*.csproj

# 2. Kopiranje ostalih izvornih fajlova i bildanje
COPY server/api/SeharaCloud.api/ ./server/api/SeharaCloud.api/
RUN dotnet publish ./server/api/SeharaCloud.api/*.csproj -c Release -o /app/publish --no-restore

# === STAGE 2: Runtime ===
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

COPY --from=build /app/publish .

EXPOSE 5000
ENV ASPNETCORE_URLS=http://+:5000

# Pazite na tačno ime .dll fajla (da li je SeharaCloud.Api.dll ili SeharaCloud.api.dll)
ENTRYPOINT ["dotnet", "SeharaCloud.api.dll"]
