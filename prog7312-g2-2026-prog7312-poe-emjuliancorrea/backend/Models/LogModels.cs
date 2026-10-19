using System.Text.Json.Serialization;

namespace Smart_X.backend.Models;

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum LogClassification
{
    Diagnostic,
    Error,
    Calibration,
    Firmware,
    Configuration,
    RawData,
    Other
}
public record ClassifyRequest(string FileName, string? Sample);

public record ClassificationResult(LogClassification Classification, string Reason);