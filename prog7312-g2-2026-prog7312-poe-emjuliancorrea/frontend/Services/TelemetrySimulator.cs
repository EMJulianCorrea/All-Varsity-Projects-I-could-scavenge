using Smart_X.frontend.Models;

namespace Smart_X.frontend.Services;

public static class TelemetrySimulator
{
    public static TelemetryReading Generate(SensorRecord sensor, Random rng, double anomalyChance = 0.15)
    {
        var isAnomalous = rng.NextDouble() < anomalyChance;

        var baseValue = sensor.Category switch
        {
            SensorCategory.Temperature => 21.0,
            SensorCategory.Humidity => 45.0,
            SensorCategory.Pressure => 1013.0,
            SensorCategory.Motion => 1.0,
            SensorCategory.Vibration => 0.2,
            _ => 10.0
        };

        var units = sensor.Category switch
        {
            SensorCategory.Temperature => "°C",
            SensorCategory.Humidity => "%RH",
            SensorCategory.Pressure => "hPa",
            SensorCategory.Motion => "events",
            SensorCategory.Vibration => "g",
            _ => "units"
        };

        var value = isAnomalous
            ? baseValue * (1.8 + rng.NextDouble())
            : baseValue + (rng.NextDouble() * 2 - 1);

        return new TelemetryReading
        {
            SensorId = sensor.Id,
            Value = Math.Round(value, 2),
            Units = units,
            IsAnomalous = isAnomalous
        };
    }
}
