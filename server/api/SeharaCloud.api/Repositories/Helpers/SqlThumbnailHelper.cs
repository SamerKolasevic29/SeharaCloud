namespace SeharaCloud.Repositories.Helpers;

public static class SqlThumbnailHelper
{
        // Tables with only one ThumbnailI colum
        public static string Single(string baseUrl, string alias = "m") =>
    $"""
    CASE 
        WHEN {alias}.thumbnail_id IS NOT NULL
        THEN '{baseUrl}/api/thumbnail/' || {alias}.thumbnail_id
        ELSE NULL
    END
    """;

    // Tables with two ThumbnailId_n columns 
    public static string Double(string baseUrl, string alias = "g") =>
    $"""
    CASE 
        WHEN {alias}.thumbnail_id2 IS NOT NULL
        THEN '{baseUrl}/api/thumbnail/' || {alias}.thumbnail_id2
        ELSE '{baseUrl}/api/thumbnail/' || {alias}.thumbnail_id1
    END
    """;

    // Tables with three ThumbnailId_n columns   
    public static string Triple(string baseUrl, string alias = "a") =>
    $"""
    CASE 
        WHEN {alias}.thumbnail_id3 IS NOT NULL
        THEN '{baseUrl}/api/thumbnail/' || {alias}.thumbnail_id3
        WHEN {alias}.thumbnail_id2 IS NOT NULL
        THEN '{baseUrl}/api/thumbnail/' || {alias}.thumbnail_id2
        ELSE '{baseUrl}/api/thumbnail/' || {alias}.thumbnail_id1
    END
    """;

}