namespace Smart_X.backend.Models;

/// <summary>
/// A reusable, generic wrapper for a single piece of telemetry. Using a generic
/// struct here (rather than a non-generic packet with an `object Value` field)
/// means the JIT emits a specialized version of this type per value type
/// (TelemetryPacket&lt;float&gt;, TelemetryPacket&lt;int&gt;, TelemetryPacket&lt;bool&gt;...),
/// so the value is never boxed onto the heap just to be stored or read back.
///
/// - float  for analog readings, e.g. temperature, humidity
/// - int    for discrete/counter metrics, e.g. power draw in watts, event counts
/// - bool   for on/off triggers, e.g. smart switch state
/// </summary>
/// <typeparam name="T">A value type — enforced by the struct constraint below.</typeparam>
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

/// <summary>
/// Convenience factory so call sites don't need to spell out the generic type argument
/// (var packet = TelemetryPacket.For("SENSOR-1", 21.4f, "°C");).
/// </summary>
public static class TelemetryPacket
{
    public static TelemetryPacket<T> For<T>(string sensorId, T value, string unit, bool isAnomalous = false) where T : struct
        => new(sensorId, value, unit, isAnomalous);
}
