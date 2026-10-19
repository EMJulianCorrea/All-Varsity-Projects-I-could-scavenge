using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using Smart_X.frontend.Models;
using System.Net.Http.Json;

namespace Smart_X.frontend.Services;

public class Smart_XApiClient
{
    private readonly HttpClient _http;

    public Smart_XApiClient(string baseUrl = "http://localhost:8080")
    {
        _http = new HttpClient { BaseAddress = new Uri(baseUrl) };
    }

    public async Task<(bool Success, string? Error, SensorRecord? Sensor)> RegisterSensorAsync(SensorRecord sensor)
    {
        try
        {
            var response = await _http.PostAsJsonAsync("/api/sensors", sensor);
            if (response.IsSuccessStatusCode)
            {
                var created = await response.Content.ReadFromJsonAsync<SensorRecord>();
                return (true, null, created);
            }

            var problem = await response.Content.ReadFromJsonAsync<ApiError>();
            return (false, problem?.Error ?? $"Request failed with status {response.StatusCode}.", null);
        }
        catch (HttpRequestException ex)
        {
            return (false, $"Could not reach the API: {ex.Message}", null);
        }
    }

    public async Task<List<SensorRecord>> GetSensorsAsync()
    {
        try
        {
            var result = await _http.GetFromJsonAsync<List<SensorRecord>>("/api/sensors");
            return result ?? new List<SensorRecord>();
        }
        catch (HttpRequestException)
        {
            return new List<SensorRecord>();
        }
    }

    public async Task<(bool Success, string? Error, SensorRecord? Sensor)> UpdateSensorAsync(string id, SensorRecord sensor)
    {
        try
        {
            var response = await _http.PutAsJsonAsync($"/api/sensors/{id}", sensor);
            if (response.IsSuccessStatusCode)
            {
                var updated = await response.Content.ReadFromJsonAsync<SensorRecord>();
                return (true, null, updated);
            }

            var problem = await response.Content.ReadFromJsonAsync<ApiError>();
            return (false, problem?.Error ?? $"Request failed with status {response.StatusCode}.", null);
        }
        catch (HttpRequestException ex)
        {
            return (false, $"Could not reach the API: {ex.Message}", null);
        }
    }

    public async Task<(bool Success, string? Error)> DeleteSensorAsync(string id)
    {
        try
        {
            var response = await _http.DeleteAsync($"/api/sensors/{id}");
            if (response.IsSuccessStatusCode) return (true, null);

            var problem = await response.Content.ReadFromJsonAsync<ApiError>();
            return (false, problem?.Error ?? $"Delete failed with status {response.StatusCode}.");
        }
        catch (HttpRequestException ex)
        {
            return (false, $"Could not reach the API: {ex.Message}");
        }
    }

    public async Task<(bool Success, string? Error)> SendTelemetryAsync(TelemetryReading reading)
    {
        try
        {
            var response = await _http.PostAsJsonAsync("/api/telemetry", reading);
            if (response.IsSuccessStatusCode) return (true, null);

            var problem = await response.Content.ReadFromJsonAsync<ApiError>();
            return (false, problem?.Error ?? $"Request failed with status {response.StatusCode}.");
        }
        catch (HttpRequestException ex)
        {
            return (false, $"Could not reach the API: {ex.Message}");
        }
    }

    public async Task<(bool Success, string? Error)> UploadLogAsync(string sensorId, string filePath)
    {
        try
        {
            using var content = new MultipartFormDataContent();
            var bytes = await File.ReadAllBytesAsync(filePath);
            var fileContent = new ByteArrayContent(bytes);
            fileContent.Headers.ContentType = new MediaTypeHeaderValue("application/octet-stream");
            content.Add(fileContent, "file", Path.GetFileName(filePath));

            var response = await _http.PostAsync($"/api/sensors/{sensorId}/logs", content);
            if (response.IsSuccessStatusCode) return (true, null);

            var problem = await response.Content.ReadFromJsonAsync<ApiError>();
            return (false, problem?.Error ?? $"Upload failed with status {response.StatusCode}.");
        }
        catch (HttpRequestException ex)
        {
            return (false, $"Could not reach the API: {ex.Message}");
        }
        catch (IOException ex)
        {
            return (false, $"Could not read the file: {ex.Message}");
        }
    }
}
