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
    public string Unit { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; }
    public bool IsAnomalous { get; set; }
}

public class SensorLog
{
    public Guid LogId { get; set; }
    public string SensorId { get; set; } = string.Empty;
    public string FileName { get; set; } = string.Empty;
    public long SizeBytes { get; set; }
    public DateTime UploadedAt { get; set; }
}

public class ApiError
{
    public string? Error { get; set; }
}
