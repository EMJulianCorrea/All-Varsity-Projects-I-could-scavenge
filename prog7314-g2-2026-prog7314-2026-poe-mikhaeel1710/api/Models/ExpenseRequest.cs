namespace DriveMate.Api.Models;

public class ExpenseRequest
{
    public int VehicleId { get; set; }

    public string Category { get; set; } = string.Empty;

    public decimal Amount { get; set; }

    public DateOnly Date { get; set; }

    public string? Description { get; set; }
}