namespace Smart_X.backend.Models;

/// <summary>
/// Represents a single sensor's load/power reading. Operator overloads let you
/// combine or compare readings directly — e.g. `smartSwitch1 + smartSwitch2` to
/// get the aggregate load across two smart sensors — instead of manually pulling
/// out .Watts on each side everywhere this comes up.
/// </summary>
public readonly struct SensorLoad
{
    public string SensorId { get; }
    public double Watts { get; }

    public SensorLoad(string sensorId, double watts)
    {
        SensorId = sensorId;
        Watts = watts;
    }

    // Aggregate load of two sensors, e.g.: var total = smartSwitch1Load + smartSwitch2Load;
    public static SensorLoad operator +(SensorLoad a, SensorLoad b)
        => new($"{a.SensorId}+{b.SensorId}", a.Watts + b.Watts);

    // Difference in load between two sensors.
    public static SensorLoad operator -(SensorLoad a, SensorLoad b)
        => new($"{a.SensorId}-{b.SensorId}", a.Watts - b.Watts);

    // Scale a load, e.g. estimating N identical sensors: singleUnitLoad * 5
    public static SensorLoad operator *(SensorLoad a, double factor)
        => new(a.SensorId, a.Watts * factor);

    // Threshold-style comparisons, e.g.: if (aggregateLoad > circuitLimit) ...
    public static bool operator >(SensorLoad a, SensorLoad b) => a.Watts > b.Watts;
    public static bool operator <(SensorLoad a, SensorLoad b) => a.Watts < b.Watts;
    public static bool operator >=(SensorLoad a, SensorLoad b) => a.Watts >= b.Watts;
    public static bool operator <=(SensorLoad a, SensorLoad b) => a.Watts <= b.Watts;

    public override string ToString() => $"{SensorId}: {Watts:F1}W";
}

/// <summary>Request body for POST /api/sensors/aggregate-load — minimal APIs only bind
/// one complex parameter from the JSON body, so both loads are wrapped here.</summary>
public record AggregateLoadRequest(SensorLoad A, SensorLoad B);
