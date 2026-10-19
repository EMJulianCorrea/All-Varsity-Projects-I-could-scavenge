namespace Smart_X.backend.Models;
public readonly struct SensorLoad
{
    public string SensorId { get; }
    public double Watts { get; }

    public SensorLoad(string sensorId, double watts)
    {
        SensorId = sensorId;
        Watts = watts;
    }

    public static SensorLoad operator +(SensorLoad a, SensorLoad b)
        => new($"{a.SensorId}+{b.SensorId}", a.Watts + b.Watts);

    public static SensorLoad operator -(SensorLoad a, SensorLoad b)
        => new($"{a.SensorId}-{b.SensorId}", a.Watts - b.Watts);

    public static SensorLoad operator *(SensorLoad a, double factor)
        => new(a.SensorId, a.Watts * factor);

    public static bool operator >(SensorLoad a, SensorLoad b) => a.Watts > b.Watts;
    public static bool operator <(SensorLoad a, SensorLoad b) => a.Watts < b.Watts;
    public static bool operator >=(SensorLoad a, SensorLoad b) => a.Watts >= b.Watts;
    public static bool operator <=(SensorLoad a, SensorLoad b) => a.Watts <= b.Watts;

    public override string ToString() => $"{SensorId}: {Watts:F1}W";
}

public record AggregateLoadRequest(SensorLoad A, SensorLoad B);
