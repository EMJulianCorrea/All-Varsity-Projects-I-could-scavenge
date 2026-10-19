using DriveMate.Api.Models;
using DriveMate.Api.Services;

namespace DriveMate.Api.Tests;

public class VehicleValidatorTests
{
    private const int CurrentYear = 2026;

    private static CreateVehicleRequest CreateValidRequest()
    {
        return new CreateVehicleRequest
        {
            FirebaseUserId = "test-user",
            Nickname = "Daily Car",
            Make = "Toyota",
            Model = "Corolla",
            Year = 2022,
            RegistrationNumber = "ND 123-456",
            FuelType = "Petrol",
            OdometerKm = 45000,
            LicenceRenewalDate =
                new DateTime(
                    2026,
                    12,
                    1,
                    0,
                    0,
                    0,
                    DateTimeKind.Utc)
        };
    }

    [Fact]
    public void ValidVehicle_ReturnsNoValidationError()
    {
        var request = CreateValidRequest();

        var result =
            VehicleValidator.Validate(
                request,
                CurrentYear);

        Assert.Null(result);
    }

    [Fact]
    public void MissingMake_ReturnsRequiredInformationError()
    {
        var request = CreateValidRequest();
        request.Make = "";

        var result =
            VehicleValidator.Validate(
                request,
                CurrentYear);

        Assert.Equal(
            "Required vehicle information is missing.",
            result);
    }

    [Fact]
    public void MissingRegistration_ReturnsRequiredInformationError()
    {
        var request = CreateValidRequest();
        request.RegistrationNumber = " ";

        var result =
            VehicleValidator.Validate(
                request,
                CurrentYear);

        Assert.Equal(
            "Required vehicle information is missing.",
            result);
    }

    [Fact]
    public void VehicleBefore1900_ReturnsInvalidYearError()
    {
        var request = CreateValidRequest();
        request.Year = 1899;

        var result =
            VehicleValidator.Validate(
                request,
                CurrentYear);

        Assert.Equal(
            "Please enter a valid vehicle year.",
            result);
    }

    [Fact]
    public void VehicleMoreThanOneYearInFuture_ReturnsInvalidYearError()
    {
        var request = CreateValidRequest();
        request.Year = 2028;

        var result =
            VehicleValidator.Validate(
                request,
                CurrentYear);

        Assert.Equal(
            "Please enter a valid vehicle year.",
            result);
    }

    [Fact]
    public void NextYearVehicle_IsAccepted()
    {
        var request = CreateValidRequest();
        request.Year = 2027;

        var result =
            VehicleValidator.Validate(
                request,
                CurrentYear);

        Assert.Null(result);
    }

    [Fact]
    public void NegativeOdometer_ReturnsOdometerError()
    {
        var request = CreateValidRequest();
        request.OdometerKm = -1;

        var result =
            VehicleValidator.Validate(
                request,
                CurrentYear);

        Assert.Equal(
            "Odometer cannot be negative.",
            result);
    }

    [Fact]
    public void ZeroOdometer_IsAccepted()
    {
        var request = CreateValidRequest();
        request.OdometerKm = 0;

        var result =
            VehicleValidator.Validate(
                request,
                CurrentYear);

        Assert.Null(result);
    }
}