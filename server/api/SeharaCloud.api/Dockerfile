# === STAGE 1: Build ===
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

COPY server/SeharaCloud.Api/HomeCloud.Api/*.csproj ./server/SeharaCloud.Api/HomeCloud.Api/
RUN dotnet restore ./server/SeharaCloud.Api/HomeCloud.Api/HomeCloud.Api.csproj

COPY server/SeharaCloud.Api/ ./server/SeharaCloud.Api/
RUN dotnet publish server/SeharaCloud.Api/HomeCloud.Api/HomeCloud.Api.csproj -c Release -o /app/publish --no-restore

# === STAGE 2: Runtime ===
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

COPY --from=build /app/publish .

EXPOSE 5000
ENV ASPNETCORE_URLS=http://+:5000

ENTRYPOINT ["dotnet", "SeharaCloud.Api.dll"]