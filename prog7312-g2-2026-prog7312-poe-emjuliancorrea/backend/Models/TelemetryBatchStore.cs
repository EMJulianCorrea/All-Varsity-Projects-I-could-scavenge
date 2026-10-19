namespace Smart_X.backend.Models;
public class TelemetryBatchStore
{
    private readonly List<double[,]> _batches = new();
    private readonly List<string[]> _batchSensorIds = new();

    public void AddBatch(string[] sensorIds, double[,] readings)
    {
        if (readings.GetLength(0) != sensorIds.Length)
            throw new ArgumentException("Row count of the readings array must match sensorIds.Length.");

        _batches.Add(readings);
        _batchSensorIds.Add(sensorIds);
    }

    public IReadOnlyList<double[,]> Batches => _batches;

    public int BatchCount => _batches.Count;

    public double GetReading(int batchIndex, int sensorRow, int readingColumn)
        => _batches[batchIndex][sensorRow, readingColumn];

    public double GetSensorAverage(int batchIndex, int sensorRow)
    {
        var batch = _batches[batchIndex];
        double sum = 0;
        var columns = batch.GetLength(1);
        for (var c = 0; c < columns; c++)
            sum += batch[sensorRow, c];
        return sum / columns;
    }

   
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
