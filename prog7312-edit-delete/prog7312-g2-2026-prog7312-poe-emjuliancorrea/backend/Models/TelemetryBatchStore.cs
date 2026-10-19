namespace Smart_X.backend.Models;

/// <summary>
/// Stores telemetry history as batches. Each batch is a 2D array —
/// rows = sensors read during that polling cycle, columns = successive readings
/// taken for each sensor within the batch window. Batches themselves are appended
/// to a List&lt;double[,]&gt;, so you get the fixed, fast, rectangular layout of a
/// multi-dimensional array *within* a batch, and the flexible, growable
/// collection semantics of List&lt;T&gt; *across* batches over time.
/// </summary>
public class TelemetryBatchStore
{
    private readonly List<double[,]> _batches = new();
    private readonly List<string[]> _batchSensorIds = new(); // row labels per batch, parallel to _batches

    /// <summary>
    /// Adds one batch. `readings[s, r]` = the r-th reading for the s-th sensor
    /// in this batch, where sensorIds[s] identifies that row's sensor.
    /// </summary>
    public void AddBatch(string[] sensorIds, double[,] readings)
    {
        if (readings.GetLength(0) != sensorIds.Length)
            throw new ArgumentException("Row count of the readings array must match sensorIds.Length.");

        _batches.Add(readings);
        _batchSensorIds.Add(sensorIds);
    }

    public IReadOnlyList<double[,]> Batches => _batches;

    public int BatchCount => _batches.Count;

    /// <summary>Single reading lookup: batch index, sensor row, reading column.</summary>
    public double GetReading(int batchIndex, int sensorRow, int readingColumn)
        => _batches[batchIndex][sensorRow, readingColumn];

    /// <summary>Average of every reading for one sensor within one batch.</summary>
    public double GetSensorAverage(int batchIndex, int sensorRow)
    {
        var batch = _batches[batchIndex];
        double sum = 0;
        var columns = batch.GetLength(1);
        for (var c = 0; c < columns; c++)
            sum += batch[sensorRow, c];
        return sum / columns;
    }

    /// <summary>Flattens one batch into (sensorId, reading) pairs, e.g. for sending to the API/UI.</summary>
    public IEnumerable<(string SensorId, double[] Readings)> GetBatch(int batchIndex)
    {
        var batch = _batches[batchIndex];
        var ids = _batchSensorIds[batchIndex];
        var rows = batch.GetLength(0);
        var cols = batch.GetLength(1);

        for (var r = 0; r < rows; r++)
        {
            var readings = new double[cols];
            for (var c = 0; c < cols; c++)
                readings[c] = batch[r, c];

            yield return (ids[r], readings);
        }
    }
}
