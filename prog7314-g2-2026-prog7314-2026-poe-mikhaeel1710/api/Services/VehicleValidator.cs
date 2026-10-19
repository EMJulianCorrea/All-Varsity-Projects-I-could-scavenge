using DriveMate.Api.Models;

namespace DriveMate.Api.Services;

public static class VehicleValidator
{
    public static string? Validate(
        CreateVehicleRequest request,
        int currentYear)
    {
        if (string.IsNullOrWhiteSpace(request.FirebaseUserId) ||
            string.IsNullOrWhiteSpace(request.Make) ||
            string.IsNullOrWhiteSpace(request.Model) ||
            string.IsNullOrWhiteSpace(request.RegistrationNumber) ||
            string.IsNullOrWhiteSpace(request.FuelType))
        {
            return "Required vehicle information is missing.";
        }

        if (request.Year < 1900 ||
            request.Year > currentYear + 1)
        {
            return "Please enter a valid vehicle year.";
        }

        if (request.OdometerKm < 0)
        {
            return "Odometer cannot be negative.";
        }

        return null;
    }
}