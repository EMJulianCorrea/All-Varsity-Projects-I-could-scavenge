using System.IO;
using Smart_X.backend.Models;

var builder = WebApplication.CreateBuilder(args);

// Allow the WPF client to call the API.
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
        policy.AllowAnyOrigin().AllowAnyMethod().AllowAnyHeader());
});

// Swagger/OpenAPI — gives you an actual browsable page at /swagger in Chrome/Edge
// instead of raw JSON at bare endpoints.
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();
app.UseCors();

app.UseSwagger();
app.UseSwaggerUI(options =>
{
    options.RoutePrefix = "swagger"; // browse to http://localhost:<port>/swagger
});

// Redirect the bare root to Swagger so "just opening localhost" shows something useful.
app.MapGet("/", () => Results.Redirect("/swagger"));

var sensors = new Dictionary<string, SensorRecord>();
var telemetry = new List<TelemetryReading>();
var logs = new List<SensorLog>();
var logsStorageRoot = Path.Combine(AppContext.BaseDirectory, "sensor-logs");
Directory.CreateDirectory(logsStorageRoot);

app.MapGet("/api/health", () => Results.Ok(new { status = "OK", time = DateTime.UtcNow }));

// Sensor registration
app.MapPost("/api/sensors", (SensorRecord incoming) =>
{
    if (string.IsNullOrWhiteSpace(incoming.Id))
        return Results.BadRequest(new { error = "Sensor ID required." });

    if (sensors.ContainsKey(incoming.Id))
        return Results.Conflict(new { error = $"Sensor ID '{incoming.Id}' already exists." });

    if (string.IsNullOrWhiteSpace(incoming.DeploymentLocation))
        return Results.BadRequest(new { error = "Deployment location required." });

    incoming.RegisteredAt = DateTime.UtcNow;
    incoming.LastStatus = "Registered";
    sensors[incoming.Id] = incoming;

    return Results.Created($"/api/sensors/{incoming.Id}", incoming);
});

app.MapGet("/api/sensors", () => Results.Ok(sensors.Values.OrderByDescending(s => s.RegisteredAt)));

app.MapGet("/api/sensors/{id}", (string id) =>
    sensors.TryGetValue(id, out var sensor) ? Results.Ok(sensor) : Results.NotFound());

app.MapDelete("/api/sensors/{id}", (string id) =>
    sensors.Remove(id) ? Results.NoContent() : Results.NotFound());

// Edit an existing sensor. ID is immutable — only location/category/simulated flag can change.
app.MapPut("/api/sensors/{id}", (string id, SensorRecord updated) =>
{
    if (!sensors.TryGetValue(id, out var existing))
        return Results.NotFound(new { error = $"Sensor '{id}' is not registered." });

    if (string.IsNullOrWhiteSpace(updated.DeploymentLocation))
        return Results.BadRequest(new { error = "Deployment location required." });

    existing.DeploymentLocation = updated.DeploymentLocation;
    existing.Category = updated.Category;
    existing.IsSimulated = updated.IsSimulated;

    return Results.Ok(existing);
});

// Telemetry ingestion
app.MapPost("/api/telemetry", (TelemetryReading reading) =>
{
    if (!sensors.ContainsKey(reading.SensorId))
        return Results.BadRequest(new { error = $"Unknown sensor '{reading.SensorId}'. Register it before sending telemetry." });

    reading.Timestamp = DateTime.UtcNow;
    telemetry.Add(reading);
    sensors[reading.SensorId].LastStatus = reading.IsAnomalous ? "Anomaly detected" : "Nominal";

    return Results.Created($"/api/telemetry/{reading.SensorId}", reading);
});

app.MapGet("/api/telemetry/{sensorId}", (string sensorId, int take = 50) =>
    Results.Ok(telemetry.Where(t => t.SensorId == sensorId)
                         .OrderByDescending(t => t.Timestamp)
                         .Take(take)));

app.MapGet("/api/telemetry", (int take = 100) =>
    Results.Ok(telemetry.OrderByDescending(t => t.Timestamp).Take(take)));

// Hardware log attachment
app.MapPost("/api/sensors/{id}/logs", async (string id, HttpRequest request) =>
{
    if (!sensors.ContainsKey(id))
        return Results.NotFound(new { error = $"Sensor '{id}' is not registered." });

    if (!request.HasFormContentType)
        return Results.BadRequest(new { error = "Expected form-data with file type 'file'." });

    var form = await request.ReadFormAsync();
    var file = form.Files["file"];
    if (file is null || file.Length == 0)
        return Results.BadRequest(new { error = "No file was provided." });

    var sensorFolder = Path.Combine(logsStorageRoot, id);
    Directory.CreateDirectory(sensorFolder);
    var storedName = $"{DateTime.UtcNow:yyyyMMdd_HHmmss}_{file.FileName}";
    var storedPath = Path.Combine(sensorFolder, storedName);

    await using (var stream = File.Create(storedPath))
        await file.CopyToAsync(stream);

    var log = new SensorLog
    {
        SensorId = id,
        FileName = file.FileName,
        SizeInBytes = file.Length,
        StoredPath = storedPath
    };
    logs.Add(log);

    return Results.Created($"/api/sensors/{id}/logs/{log.LogId}", log);
});

app.MapGet("/api/sensors/{id}/logs", (string id) =>
    Results.Ok(logs.Where(l => l.SensorId == id).OrderByDescending(l => l.UploadedAt)));

// ---- Demo: TelemetryPacket<T> — generic, boxing-free wrapper for mixed sensor payloads ----
app.MapGet("/api/demo/telemetry-packet", () =>
{
    var temperature = TelemetryPacket.For("TEMP-01", 21.4f, "°C");          // float
    var powerDraw = TelemetryPacket.For("PWR-01", 1450, "W");               // int
    var switchState = TelemetryPacket.For("SWITCH-01", true, "on/off");     // bool

    return Results.Ok(new
    {
        temperature = temperature.ToString(),
        powerDraw = powerDraw.ToString(),
        switchState = switchState.ToString()
    });
});

// ---- Demo: operator overloading on SensorLoad — direct aggregation of two sensors ----
app.MapPost("/api/sensors/aggregate-load", (AggregateLoadRequest request) =>
{
    var a = request.A;
    var b = request.B;
    var aggregate = a + b; // overloaded '+'
    var difference = a - b; // overloaded '-'
    var aIsHigher = a > b;  // overloaded '>'

    return Results.Ok(new
    {
        aggregate = aggregate.ToString(),
        difference = difference.ToString(),
        aIsHigherThanB = aIsHigher
    });
});

// ---- Demo: multi-dimensional arrays of historical batches, stored in a List<T> ----
var batchStore = new TelemetryBatchStore();
app.MapPost("/api/telemetry/batches/demo", () =>
{
    // A batch: 2 sensors (rows) x 4 successive readings each (columns).
    var readings = new double[,]
    {
        { 21.0, 21.3, 21.1, 21.6 }, // TEMP-01
        { 44.0, 45.2, 44.8, 46.1 }  // HUM-01
    };
    batchStore.AddBatch(new[] { "TEMP-01", "HUM-01" }, readings);

    return Results.Ok(new
    {
        totalBatches = batchStore.BatchCount,
        latestBatch = batchStore.GetBatch(batchStore.BatchCount - 1)
            .Select(s => new { s.SensorId, s.Readings }),
        temp01AverageInLatestBatch = batchStore.GetSensorAverage(batchStore.BatchCount - 1, sensorRow: 0)
    });
});

// ---- Demo: recursive validation of a Facility > Zone > Sub-Zone deployment tree ----
app.MapPost("/api/deployment/validate", (DeploymentNode root) =>
{
    var isValid = DeploymentTreeValidator.Validate(root, out var errors);
    var totalSensors = DeploymentTreeValidator.CountSensors(root);

    return Results.Ok(new { isValid, errors, totalSensors });
});

app.MapFallback(() => Results.Json(new { error = "Route not found" }, statusCode: 404));

app.Run();
