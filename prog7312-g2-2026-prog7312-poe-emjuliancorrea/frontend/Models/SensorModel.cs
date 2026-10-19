using System.Text.Json.Serialization;

namespace Smart_X.frontend.Models;

public enum SensorCategory
{
    Temperature,
    Humidity,
    Pressure,
    Motion,
    Vibration,
    Other
}

public class SensorRecord
{
    public string Id { get; set; } = string.Empty;
    public string DeploymentLocation { get; set; } = string.Empty;
    public SensorCategory Category { get; set; }
    public DateTime RegisteredAt { get; set; }
    public bool IsSimulated { get; set; }
    public string? LastStatus { get; set; }

    public override string ToString() => $"{Id}  ·  {DeploymentLocation}  ·  {Category}";
}

public class TelemetryReading
{
    public string SensorId { get; set; } = string.Empty;
    public double Value { get; set; }
    public string Units { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; }
    public bool IsAnomalous { get; set; }
}

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

public class SensorLog
{
    public Guid LogId { get; set; }
    public string SensorId { get; set; } = string.Empty;
    public string FileName { get; set; } = string.Empty;
    public long SizeInBytes { get; set; }
    public DateTime UploadedAt { get; set; }
    public LogClassification Classification { get; set; }

    public string ClassificationText => Classification == LogClassification.RawData ? "Raw Data" : Classification.ToString();
    public string UploadedText => UploadedAt.ToLocalTime().ToString("yyyy-MM-dd HH:mm");
    public string SizeText => SizeInBytes switch
    {
        < 1024 => $"{SizeInBytes} B",
        < 1024 * 1024 => $"{SizeInBytes / 1024.0:0.#} KB",
        _ => $"{SizeInBytes / 1024.0 / 1024.0:0.#} MB"
    };
}

public class ClassifyRequest
{
    public string FileName { get; set; } = string.Empty;
    public string? Sample { get; set; }
}

public class ClassificationResult
{
    public LogClassification Classification { get; set; }
    public string Reason { get; set; } = string.Empty;
}

public class ApiError
{
    public string? Error { get; set; }
}

public class StreamEntry
{
    public string SensorId { get; set; } = string.Empty;
    public string Category { get; set; } = string.Empty;
    public double Value { get; set; }
    public string Units { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; }
    public bool IsAnomalous { get; set; }

    public string TimeText => Timestamp.ToLocalTime().ToString("HH:mm:ss.fff");
    public string ValueText => $"{Value:0.##} {Units}";
    public string StatusText => IsAnomalous ? "ANOMALY" : "OK";
}

public class AlertEntry
{
    public Guid Id { get; set; }
    public string SensorId { get; set; } = string.Empty;
    public string Message { get; set; } = string.Empty;
    public int Severity { get; set; }
    public DateTime Timestamp { get; set; }

    public string SeverityLabel => Severity <= 1 ? "CRITICAL" : "WARNING";
}

public class OverrideCommand
{
    public Guid Id { get; set; }
    public string SensorId { get; set; } = string.Empty;
    public string Command { get; set; } = string.Empty;
    public string? PreviousStatus { get; set; }
    public string NewStatus { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; }

    public string Display => $"{Timestamp.ToLocalTime():HH:mm:ss}  {Command} \u2192 {SensorId}";
}

public class OverrideRequest
{
    public string SensorId { get; set; } = string.Empty;
    public string Command { get; set; } = string.Empty;
}

public class BatchResult
{
    public int Accepted { get; set; }
    public int Rejected { get; set; }
    public List<string> Errors { get; set; } = new();
}

public class StreamSnapshot
{
    public int QueueDepth { get; set; }
    public long TotalProcessed { get; set; }
    public int TotalSensors { get; set; }
    public List<SensorRecord> Sensors { get; set; } = new();
    public List<StreamEntry> Recent { get; set; } = new();
    public List<AlertEntry> Alerts { get; set; } = new();
    public List<string> ActiveErrors { get; set; } = new();
    public List<OverrideCommand> Overrides { get; set; } = new();
}
