using Smart_X.backend.Models;

namespace Smart_X.backend.Services;

public class CommandStreamService
{
    public static readonly string[] ValidCommands = { "Reset", "Calibrate", "Disable", "Enable" };
    private const int MaxLogEntries = 2000;

    private readonly Dictionary<string, SensorRecord> _devices;
    private readonly Queue<TelemetryReading> _ingestQueue = new();
    private readonly PriorityQueue<AlertEntry, (int Severity, long Ticks)> _alerts = new();
    private readonly Stack<OverrideCommand> _overrides = new();
    private readonly SortedDictionary<DateTime, List<TelemetryReading>> _log = new();
    private readonly HashSet<string> _activeErrors = new();
    private readonly HashSet<string> _disabledSensors = new();

    private int _logCount;
    private long _totalProcessed;

    public CommandStreamService(Dictionary<string, SensorRecord> devices) => _devices = devices;

    private object Gate => _devices;
    private static string ErrorKey(string sensorId) => $"{sensorId}:ANOMALY";

    public (IngestOutcome Outcome, string? Error) Ingest(TelemetryReading reading)
    {
        lock (Gate)
        {
            var result = Enqueue(reading);
            if (result.Outcome == IngestOutcome.Accepted) ProcessPending();
            return result;
        }
    }

    public BatchResult IngestBatch(IEnumerable<TelemetryReading> readings)
    {
        lock (Gate)
        {
            int accepted = 0, rejected = 0;
            var errors = new List<string>();

            foreach (var reading in readings)
            {
                var (outcome, error) = Enqueue(reading);
                if (outcome == IngestOutcome.Accepted) { accepted++; continue; }

                rejected++;
                if (error is not null && errors.Count < 10 && !errors.Contains(error)) errors.Add(error);
            }

            ProcessPending();
            return new BatchResult(accepted, rejected, errors);
        }
    }

    private (IngestOutcome Outcome, string? Error) Enqueue(TelemetryReading reading)
    {
        if (!_devices.TryGetValue(reading.SensorId ?? string.Empty, out var device))   // O(1)
            return (IngestOutcome.UnknownSensor, $"Unknown sensor '{reading.SensorId}'. Register it before sending telemetry.");

        if (_disabledSensors.Contains(device.Id))
            return (IngestOutcome.SensorDisabled, $"Sensor '{device.Id}' is disabled by a manual override. Enable it to resume telemetry.");

        reading.Timestamp = DateTime.UtcNow;
        _ingestQueue.Enqueue(reading);
        return (IngestOutcome.Accepted, null);
    }

    private void ProcessPending()
    {
        while (_ingestQueue.TryDequeue(out var reading))
        {
            if (!_devices.TryGetValue(reading.SensorId, out var device)) continue;

            AddToLog(reading);
            _totalProcessed++;

            var errorKey = ErrorKey(device.Id);
            if (reading.IsAnomalous)
            {
                var firstOccurrence = _activeErrors.Add(errorKey);
                var severity = firstOccurrence ? 2 : 1;
                var message = firstOccurrence
                    ? $"Anomalous reading: {reading.Value:0.##} {reading.Units}"
                    : $"Repeated anomaly ({reading.Value:0.##} {reading.Units}), still unresolved";

                _alerts.Enqueue(
                    new AlertEntry(Guid.NewGuid(), device.Id, message, severity, reading.Timestamp),
                    (severity, reading.Timestamp.Ticks));

                device.LastStatus = "Anomaly detected";
            }
            else
            {
                _activeErrors.Remove(errorKey);
                device.LastStatus = "Nominal";
            }
        }
    }

    private void AddToLog(TelemetryReading reading)
    {
        if (!_log.TryGetValue(reading.Timestamp, out var bucket))
            _log[reading.Timestamp] = bucket = new List<TelemetryReading>();
        bucket.Add(reading);
        _logCount++;
        while (_logCount > MaxLogEntries)
        {
            var oldest = _log.First();
            _logCount -= oldest.Value.Count;
            _log.Remove(oldest.Key);
        }
    }

    private IEnumerable<TelemetryReading> NewestFirst()
    {
        foreach (var bucket in _log.Values.Reverse())
            for (var i = bucket.Count - 1; i >= 0; i--)
                yield return bucket[i];
    }

    public List<TelemetryReading> GetRecent(int take, string? sensorId = null)
    {
        lock (Gate)
        {
            return NewestFirst()
                .Where(r => sensorId is null || r.SensorId == sensorId)
                .Take(take)
                .ToList();
        }
    }

    private static bool Matches(SensorRecord sensor, string? filter) =>
        string.IsNullOrWhiteSpace(filter) ||
        sensor.Category.ToString().Contains(filter.Trim(), StringComparison.OrdinalIgnoreCase);

    public StreamSnapshot GetSnapshot(string? categoryFilter, int recentTake = 40)
    {
        lock (Gate)
        {
            var sensors = _devices.Values
                .Where(s => Matches(s, categoryFilter))
                .OrderBy(s => s.Id)
                .ToList();

            var recent = new List<StreamEntry>(recentTake);
            foreach (var reading in NewestFirst())
            {
                if (recent.Count >= recentTake) break;
                if (_devices.TryGetValue(reading.SensorId, out var device) && Matches(device, categoryFilter))
                    recent.Add(new StreamEntry(reading.SensorId, device.Category.ToString(), reading.Value,
                                               reading.Units, reading.Timestamp, reading.IsAnomalous));
            }

            var alerts = _alerts.UnorderedItems
                .OrderBy(x => x.Priority)
                .Select(x => x.Element)
                .Where(a => _devices.TryGetValue(a.SensorId, out var d) && Matches(d, categoryFilter))
                .Take(25)
                .ToList();

            var errors = _activeErrors
                .Where(key =>
                {
                    var sensorId = key[..key.LastIndexOf(':')];
                    return _devices.TryGetValue(sensorId, out var d) && Matches(d, categoryFilter);
                })
                .OrderBy(key => key)
                .ToList();

            return new StreamSnapshot(
                _ingestQueue.Count,
                _totalProcessed,
                _devices.Count,
                sensors,
                recent,
                alerts,
                errors,
                _overrides.Take(15).ToList());  
        }
    }

    public (OverrideCommand? Command, string? Error, bool SensorMissing) ApplyOverride(string sensorId, string command)
    {
        lock (Gate)
        {
            var canonical = ValidCommands.FirstOrDefault(c => c.Equals(command, StringComparison.OrdinalIgnoreCase));
            if (canonical is null)
                return (null, $"Unknown command '{command}'. Valid commands: {string.Join(", ", ValidCommands)}.", false);

            if (!_devices.TryGetValue(sensorId ?? string.Empty, out var device))
                return (null, $"Sensor '{sensorId}' is not registered.", true);

            var cmd = new OverrideCommand
            {
                SensorId = device.Id,
                Command = canonical,
                PreviousStatus = device.LastStatus,
                PreviouslyDisabled = _disabledSensors.Contains(device.Id)
            };

            switch (canonical)
            {
                case "Reset":
                    cmd.NewStatus = "Reset (manual override)";
                    var key = ErrorKey(device.Id);
                    if (_activeErrors.Remove(key)) cmd.ClearedErrors.Add(key);
                    break;
                case "Calibrate":
                    cmd.NewStatus = "Calibrating (manual override)";
                    break;
                case "Disable":
                    cmd.NewStatus = "Disabled (manual override)";
                    _disabledSensors.Add(device.Id);
                    break;
                case "Enable":
                    cmd.NewStatus = "Enabled (manual override)";
                    _disabledSensors.Remove(device.Id);
                    break;
            }

            device.LastStatus = cmd.NewStatus;
            _overrides.Push(cmd);
            return (cmd, null, false);
        }
    }

    public OverrideCommand? UndoLastOverride()
    {
        lock (Gate)
        {
            if (!_overrides.TryPop(out var cmd)) return null;

            if (_devices.TryGetValue(cmd.SensorId, out var device))
            {
                device.LastStatus = cmd.PreviousStatus;
                if (cmd.PreviouslyDisabled) _disabledSensors.Add(device.Id); else _disabledSensors.Remove(device.Id);
                foreach (var key in cmd.ClearedErrors) _activeErrors.Add(key);
            }

            return cmd;
        }
    }

    public List<OverrideCommand> GetOverrideHistory(int take)
    {
        lock (Gate) return _overrides.Take(take).ToList();
    }

    public AlertEntry? AcknowledgeNextAlert()
    {
        lock (Gate)
        {
            while (_alerts.TryDequeue(out var alert, out _))
                if (_devices.ContainsKey(alert.SensorId)) return alert; 
            return null;
        }
    }

    public int ClearAlerts()
    {
        lock (Gate)
        {
            var count = _alerts.Count;
            _alerts.Clear();
            return count;
        }
    }

    public void OnSensorRemoved(string sensorId)
    {
        lock (Gate)
        {
            _disabledSensors.Remove(sensorId);
            _activeErrors.Remove(ErrorKey(sensorId));
        }
    }
}
