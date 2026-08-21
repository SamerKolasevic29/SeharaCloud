# === STAGE 1: Build ===
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# 1. Copy csproj filei n correct dir and restore packages
COPY server/api/SeharaCloud.api/*.csproj ./server/api/SeharaCloud.api/
RUN dotnet restore ./server/api/SeharaCloud.api/*.csproj

# 2. copy the rest of source file and build
COPY server/api/SeharaCloud.api/ ./server/api/SeharaCloud.api/
RUN dotnet publish ./server/api/SeharaCloud.api/*.csproj -c Release -o /app/publish --no-restore

# === STAGE 2: Runtime ===
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

COPY --from=build /app/publish .

EXPOSE 5000
ENV ASPNETCORE_URLS=http://+:5000

# Watch out for correct name of dll file
ENTRYPOINT ["dotnet", "SeharaCloud.api.dll"]
