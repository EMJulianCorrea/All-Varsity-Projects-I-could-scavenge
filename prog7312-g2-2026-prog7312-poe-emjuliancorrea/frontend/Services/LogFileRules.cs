using System.IO;

namespace Smart_X.frontend.Services;

public static class LogFileRules
{
    public const long MaxBytes = 10 * 1024 * 1024;

    public static readonly string[] AllowedExtensions =
    {
        ".log", ".txt", ".csv", ".json", ".xml", ".cfg", ".conf", ".ini",
        ".yaml", ".yml", ".bin", ".hex", ".fw", ".dat", ".zip"
    };

    public static string OpenFileFilter =>
        "Hardware logs|" + string.Join(";", AllowedExtensions.Select(e => "*" + e)) + "|All files (*.*)|*.*";

    public static string? Validate(FileInfo file)
    {
        if (!file.Exists) return "That file no longer exists.";

        var ext = file.Extension.ToLowerInvariant();
        if (!AllowedExtensions.Contains(ext))
        {
            var what = ext.Length == 0 ? "Files without an extension" : $"'{ext}' files";
            return $"{what} can't be attached. Allowed types: {string.Join(", ", AllowedExtensions)}.";
        }

        if (file.Length == 0) return "That file is empty.";
        if (file.Length > MaxBytes) return $"That file is {FormatSize(file.Length)}, which is over the {MaxBytes / 1024 / 1024} MB limit.";

        return null;
    }

    public static string FormatSize(long bytes) => bytes switch
    {
        < 1024 => $"{bytes} B",
        < 1024 * 1024 => $"{bytes / 1024.0:0.#} KB",
        _ => $"{bytes / 1024.0 / 1024.0:0.#} MB"
    };
}
