namespace DriveMate.Api.Models;

public class Vehicle
{
    public int Id { get; set; }

    public string FirebaseUserId { get; set; } = string.Empty;

    public string Nickname { get; set; } = string.Empty;

    public string Make { get; set; } = string.Empty;

    public string Model { get; set; } = string.Empty;

    public int Year { get; set; }

    public string RegistrationNumber { get; set; } = string.Empty;

    public string FuelType { get; set; } = string.Empty;

    public int OdometerKm { get; set; }

    public DateTime LicenceRenewalDate { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}