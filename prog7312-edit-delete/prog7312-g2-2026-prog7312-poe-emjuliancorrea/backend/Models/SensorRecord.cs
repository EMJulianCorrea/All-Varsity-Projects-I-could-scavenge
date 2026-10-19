namespace Smart_X.backend.Models;

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
    public string Id { get; set; } = string.Empty;              // unique identifier
    public string DeploymentLocation { get; set; } = string.Empty;
    public SensorCategory Category { get; set; }
    public DateTime RegisteredAt { get; set; } = DateTime.UtcNow;
    public bool IsSimulated { get; set; }
    public string? LastStatus { get; set; } = "Registered";
}

public class SensorLog
{
    public Guid LogId { get; set; } = Guid.NewGuid();
    public string SensorId { get; set; } = string.Empty;
    public string FileName { get; set; } = string.Empty;
    public long SizeInBytes { get; set; }
    public DateTime UploadedAt { get; set; } = DateTime.UtcNow;
    public string StoredPath { get; set; } = string.Empty;
}

public class TelemetryReading
{
    public string SensorId { get; set; } = string.Empty;
    public double Value { get; set; }
    public string Units { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; } = DateTime.UtcNow;
    public bool IsAnomalous { get; set; }
}
