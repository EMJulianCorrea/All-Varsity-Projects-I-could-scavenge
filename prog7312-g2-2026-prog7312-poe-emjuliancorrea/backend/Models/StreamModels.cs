namespace Smart_X.backend.Models;

public record AlertEntry(Guid Id, string SensorId, string Message, int Severity, DateTime Timestamp);

public record StreamEntry(string SensorId, string Category, double Value, string Units, DateTime Timestamp, bool IsAnomalous);

public class OverrideRequest
{
    public string SensorId { get; set; } = string.Empty;
    public string Command { get; set; } = string.Empty;
}

public class OverrideCommand
{
    public Guid Id { get; set; } = Guid.NewGuid();
    public string SensorId { get; set; } = string.Empty;
    public string Command { get; set; } = string.Empty;
    public string? PreviousStatus { get; set; }
    public string NewStatus { get; set; } = string.Empty;
    public bool PreviouslyDisabled { get; set; }
    public List<string> ClearedErrors { get; set; } = new();
    public DateTime Timestamp { get; set; } = DateTime.UtcNow;
}

public enum IngestOutcome { Accepted, UnknownSensor, SensorDisabled }

public record BatchResult(int Accepted, int Rejected, List<string> Errors);

public record StreamSnapshot(
    int QueueDepth,
    long TotalProcessed,
    int TotalSensors,
    List<SensorRecord> Sensors,
    List<StreamEntry> Recent,
    List<AlertEntry> Alerts,
    List<string> ActiveErrors,
    List<OverrideCommand> Overrides);
