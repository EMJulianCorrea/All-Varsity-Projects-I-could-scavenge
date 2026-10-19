namespace DriveMate.Api.Models;

public class MaintenanceRecord
{
    public string Id { get; set; } =
        Guid.NewGuid().ToString();

    public string FirebaseUserId { get; set; } =
        string.Empty;

    public string Type { get; set; } =
        string.Empty;

    public string Title { get; set; } =
        string.Empty;

    public DateOnly Date { get; set; }

    public int? MileageKm { get; set; }

    public decimal? Cost { get; set; }

    public string Notes { get; set; } =
        string.Empty;

    public DateOnly? NextDueDate { get; set; }

    public bool Completed { get; set; }

    public DateTime CreatedAt { get; set; } =
        DateTime.UtcNow;
}