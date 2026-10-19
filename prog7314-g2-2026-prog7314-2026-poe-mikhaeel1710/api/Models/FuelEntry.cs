namespace DriveMate.Api.Models;

public class FuelEntry
{
    public int Id { get; set; }

    public int VehicleId { get; set; }

    public DateOnly Date { get; set; }

    public int OdometerKm { get; set; }

    public decimal Litres { get; set; }

    public decimal? PricePerLitre { get; set; }

    public decimal TotalCost { get; set; }

    public bool IsFullTank { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}