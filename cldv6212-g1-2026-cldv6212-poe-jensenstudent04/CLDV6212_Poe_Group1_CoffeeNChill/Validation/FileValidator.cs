namespace CoffeeNChill.Functions.Validation;

public static class FileValidator
{
    private const long MaxFileSize = 10 * 1024 * 1024;

    private static readonly HashSet<string> AllowedExtensions =
        new HashSet<string>(StringComparer.OrdinalIgnoreCase) { ".pdf" };

    private static readonly HashSet<string> AllowedMimeTypes =
        new HashSet<string>(StringComparer.OrdinalIgnoreCase) { "application/pdf" };

    public static void Validate(string fileName, string contentType, long fileSize)
    {
        if (string.IsNullOrWhiteSpace(fileName))
        {
            throw new ArgumentException("A file name is required.");
        }

        string extension = Path.GetExtension(fileName);

        if (!AllowedExtensions.Contains(extension))
        {
            throw new ArgumentException("Invalid file type. Only PDF documents are allowed.");
        }

        if (!AllowedMimeTypes.Contains(contentType))
        {
            throw new ArgumentException("Invalid MIME type. Only application/pdf is allowed.");
        }

        if (fileSize <= 0)
        {
            throw new ArgumentException("The uploaded file cannot be empty.");
        }

        if (fileSize > MaxFileSize)
        {
            throw new ArgumentException("The uploaded file cannot be larger than 10 MB.");
        }
    }
}
