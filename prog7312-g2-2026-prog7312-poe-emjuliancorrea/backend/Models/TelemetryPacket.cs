namespace Smart_X.backend.Models;

public readonly struct TelemetryPacket<T> where T : struct
{
    public string SensorId { get; }
    public T Value { get; }
    public string Unit { get; }
    public DateTime Timestamp { get; }
    public bool IsAnomalous { get; }

    public TelemetryPacket(string sensorId, T value, string unit, bool isAnomalous = false, DateTime? timestamp = null)
    {
        SensorId = sensorId;
        Value = value;
        Unit = unit;
        IsAnomalous = isAnomalous;
        Timestamp = timestamp ?? DateTime.UtcNow;
    }

    public override string ToString() => $"{SensorId}: {Value}{Unit}{(IsAnomalous ? " ⚠" : "")} @ {Timestamp:HH:mm:ss}";
}

public static class TelemetryPacket
{
    public static TelemetryPacket<T> For<T>(string sensorId, T value, string unit, bool isAnomalous = false) where T : struct
        => new(sensorId, value, unit, isAnomalous);
}
