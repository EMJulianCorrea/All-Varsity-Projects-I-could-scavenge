using DriveMate.Api.Data;
using Microsoft.EntityFrameworkCore;
using DriveMate.Api.Models;
using DriveMate.Api.Services;

var builder = WebApplication.CreateBuilder(args);

var connectionString =
    builder.Configuration.GetConnectionString("DriveMateDb")
    ?? throw new InvalidOperationException(
        "Connection string 'DriveMateDb' was not found.");

builder.Services.AddDbContext<DriveMateDbContext>(options =>
    options.UseNpgsql(connectionString));

// Add services to the container.
// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseHttpsRedirection();

var summaries = new[]
{
    "Freezing", "Bracing", "Chilly", "Cool", "Mild", "Warm", "Balmy", "Hot", "Sweltering", "Scorching"
};

app.MapGet("/weatherforecast", () =>
{
    var forecast =  Enumerable.Range(1, 5).Select(index =>
        new WeatherForecast
        (
            DateOnly.FromDateTime(DateTime.Now.AddDays(index)),
            Random.Shared.Next(-20, 55),
            summaries[Random.Shared.Next(summaries.Length)]
        ))
        .ToArray();
    return forecast;
})
.WithName("GetWeatherForecast");
app.MapGet("/api/health", () =>
{
    return Results.Ok(new
    {
        status = "healthy",
        service = "DriveMate API",
        timestamp = DateTime.UtcNow
    });
});
app.MapPost("/api/cloud-test", async (
    CreateCloudTestRecordRequest request,
    DriveMateDbContext db) =>
{
    if (string.IsNullOrWhiteSpace(request.Message))
    {
        return Results.BadRequest(new
        {
            message = "Message is required."
        });
    }

    var record = new CloudTestRecord
    {
        Message = request.Message,
        CreatedAt = DateTime.UtcNow
    };

    db.CloudTestRecords.Add(record);
    await db.SaveChangesAsync();

    return Results.Created(
        $"/api/cloud-test/{record.Id}",
        record
    );
});

app.MapGet("/api/cloud-test", async (DriveMateDbContext db) =>
{
    var records = await db.CloudTestRecords
        .OrderByDescending(record => record.CreatedAt)
        .ToListAsync();

    return Results.Ok(records);
});
app.MapPost("/api/vehicles", async (
    CreateVehicleRequest request,
    DriveMateDbContext db) =>
{
  var validationError =
      VehicleValidator.Validate(
          request,
          DateTime.UtcNow.Year);

  if (validationError != null)
  {
      return Results.BadRequest(new
      {
          message = validationError
      });
  }

    var vehicle = new Vehicle
    {
        FirebaseUserId = request.FirebaseUserId.Trim(),
        Nickname = request.Nickname.Trim(),
        Make = request.Make.Trim(),
        Model = request.Model.Trim(),
        Year = request.Year,
        RegistrationNumber =
            request.RegistrationNumber.Trim().ToUpperInvariant(),
        FuelType = request.FuelType.Trim(),
        OdometerKm = request.OdometerKm,
        LicenceRenewalDate =
            request.LicenceRenewalDate.ToUniversalTime(),
        CreatedAt = DateTime.UtcNow
    };

    db.Vehicles.Add(vehicle);
    await db.SaveChangesAsync();

    return Results.Created(
        $"/api/vehicles/{vehicle.Id}",
        vehicle);
});

app.MapGet("/api/vehicles/user/{firebaseUserId}", async (
    string firebaseUserId,
    DriveMateDbContext db) =>
{
    var vehicles = await db.Vehicles
        .Where(vehicle =>
            vehicle.FirebaseUserId == firebaseUserId)
        .OrderBy(vehicle => vehicle.Make)
        .ThenBy(vehicle => vehicle.Model)
        .ToListAsync();

    return Results.Ok(vehicles);
});
app.MapGet("/api/maintenance/user/{firebaseUserId}", async (
    string firebaseUserId,
    DriveMateDbContext db) =>
{
    var records = await db.MaintenanceRecords
        .Where(record =>
            record.FirebaseUserId == firebaseUserId)
        .OrderByDescending(record => record.Date)
        .ToListAsync();

    return Results.Ok(records);
});

app.MapPost("/api/maintenance", async (
    MaintenanceRecordRequest request,
    DriveMateDbContext db) =>
{
    if (string.IsNullOrWhiteSpace(request.FirebaseUserId) ||
        string.IsNullOrWhiteSpace(request.Type) ||
        string.IsNullOrWhiteSpace(request.Title))
    {
        return Results.BadRequest(new
        {
            message = "Required maintenance information is missing."
        });
    }

    if (request.MileageKm < 0)
    {
        return Results.BadRequest(new
        {
            message = "Mileage cannot be negative."
        });
    }

    if (request.Cost < 0)
    {
        return Results.BadRequest(new
        {
            message = "Cost cannot be negative."
        });
    }

    var record = new MaintenanceRecord
    {
        FirebaseUserId =
            request.FirebaseUserId.Trim(),

        Type =
            request.Type.Trim().ToUpperInvariant(),

        Title =
            request.Title.Trim(),

        Date =
            request.Date,

        MileageKm =
            request.MileageKm,

        Cost =
            request.Cost,

        Notes =
            request.Notes.Trim(),

        NextDueDate =
            request.NextDueDate,

        Completed =
            request.Completed,

        CreatedAt =
            DateTime.UtcNow
    };

    db.MaintenanceRecords.Add(record);
    await db.SaveChangesAsync();

    return Results.Created(
        $"/api/maintenance/{record.Id}",
        record);
});

app.MapPut("/api/maintenance/{id}", async (
    string id,
    MaintenanceRecordRequest request,
    DriveMateDbContext db) =>
{
    var record =
        await db.MaintenanceRecords
            .FirstOrDefaultAsync(record =>
                record.Id == id);

    if (record == null)
    {
        return Results.NotFound();
    }

    record.Type =
        request.Type.Trim().ToUpperInvariant();

    record.Title =
        request.Title.Trim();

    record.Date =
        request.Date;

    record.MileageKm =
        request.MileageKm;

    record.Cost =
        request.Cost;

    record.Notes =
        request.Notes.Trim();

    record.NextDueDate =
        request.NextDueDate;

    record.Completed =
        request.Completed;

    await db.SaveChangesAsync();

    return Results.Ok(record);
});

app.MapDelete("/api/maintenance/{id}", async (
    string id,
    DriveMateDbContext db) =>
{
    var record =
        await db.MaintenanceRecords
            .FirstOrDefaultAsync(record =>
                record.Id == id);

    if (record == null)
    {
        return Results.NotFound();
    }

    db.MaintenanceRecords.Remove(record);
    await db.SaveChangesAsync();

    return Results.NoContent();
});
// -------------------- Running Costs: Fuel Entries --------------------

app.MapGet(
    "/api/fuel-entries/vehicle/{vehicleId:int}",
    async (int vehicleId, DriveMateDbContext db) =>
    {
        var entries = await db.FuelEntries
            .Where(entry => entry.VehicleId == vehicleId)
            .OrderByDescending(entry => entry.Date)
            .ThenByDescending(entry => entry.CreatedAt)
            .ToListAsync();

        return Results.Ok(entries);
    }
);

app.MapPost(
    "/api/fuel-entries",
    async (FuelEntryRequest request, DriveMateDbContext db) =>
    {
        if (request.VehicleId <= 0)
        {
            return Results.BadRequest("A valid vehicle ID is required.");
        }

        if (request.OdometerKm < 0)
        {
            return Results.BadRequest("Odometer cannot be negative.");
        }

        if (request.Litres <= 0)
        {
            return Results.BadRequest("Litres must be greater than zero.");
        }

        if (
            request.PricePerLitre.HasValue &&
            request.PricePerLitre.Value < 0
        )
        {
            return Results.BadRequest(
                "Price per litre cannot be negative."
            );
        }

        if (request.TotalCost < 0)
        {
            return Results.BadRequest(
                "Total cost cannot be negative."
            );
        }

        var vehicleExists = await db.Vehicles
            .AnyAsync(vehicle => vehicle.Id == request.VehicleId);

        if (!vehicleExists)
        {
            return Results.BadRequest("Vehicle does not exist.");
        }

        var entry = new FuelEntry
        {
            VehicleId = request.VehicleId,
            Date = request.Date,
            OdometerKm = request.OdometerKm,
            Litres = request.Litres,
            PricePerLitre = request.PricePerLitre,
            TotalCost = request.TotalCost,
            IsFullTank = request.IsFullTank,
            CreatedAt = DateTime.UtcNow
        };

        db.FuelEntries.Add(entry);
        await db.SaveChangesAsync();

        return Results.Created(
            $"/api/fuel-entries/{entry.Id}",
            entry
        );
    }
);

app.MapPut(
    "/api/fuel-entries/{id:int}",
    async (
        int id,
        FuelEntryRequest request,
        DriveMateDbContext db
    ) =>
    {
        var entry = await db.FuelEntries.FindAsync(id);

        if (entry == null)
        {
            return Results.NotFound();
        }

        if (
            request.VehicleId <= 0 ||
            request.OdometerKm < 0 ||
            request.Litres <= 0 ||
            request.TotalCost < 0 ||
            (
                request.PricePerLitre.HasValue &&
                request.PricePerLitre.Value < 0
            )
        )
        {
            return Results.BadRequest(
                "Invalid fuel entry values."
            );
        }

        entry.VehicleId = request.VehicleId;
        entry.Date = request.Date;
        entry.OdometerKm = request.OdometerKm;
        entry.Litres = request.Litres;
        entry.PricePerLitre = request.PricePerLitre;
        entry.TotalCost = request.TotalCost;
        entry.IsFullTank = request.IsFullTank;

        await db.SaveChangesAsync();

        return Results.Ok(entry);
    }
);

app.MapDelete(
    "/api/fuel-entries/{id:int}",
    async (int id, DriveMateDbContext db) =>
    {
        var entry = await db.FuelEntries.FindAsync(id);

        if (entry == null)
        {
            return Results.NotFound();
        }

        db.FuelEntries.Remove(entry);
        await db.SaveChangesAsync();

        return Results.NoContent();
    }
);


// -------------------- Running Costs: Expenses --------------------

app.MapGet(
    "/api/expenses/vehicle/{vehicleId:int}",
    async (int vehicleId, DriveMateDbContext db) =>
    {
        var expenses = await db.Expenses
            .Where(expense => expense.VehicleId == vehicleId)
            .OrderByDescending(expense => expense.Date)
            .ThenByDescending(expense => expense.CreatedAt)
            .ToListAsync();

        return Results.Ok(expenses);
    }
);

app.MapPost(
    "/api/expenses",
    async (ExpenseRequest request, DriveMateDbContext db) =>
    {
        if (request.VehicleId <= 0)
        {
            return Results.BadRequest("A valid vehicle ID is required.");
        }

        if (string.IsNullOrWhiteSpace(request.Category))
        {
            return Results.BadRequest(
                "Expense category is required."
            );
        }

        if (request.Amount < 0)
        {
            return Results.BadRequest(
                "Expense amount cannot be negative."
            );
        }

        var vehicleExists = await db.Vehicles
            .AnyAsync(vehicle => vehicle.Id == request.VehicleId);

        if (!vehicleExists)
        {
            return Results.BadRequest("Vehicle does not exist.");
        }

        var expense = new Expense
        {
            VehicleId = request.VehicleId,
            Category = request.Category
                .Trim()
                .ToUpperInvariant(),
            Amount = request.Amount,
            Date = request.Date,
            Description = request.Description?.Trim(),
            CreatedAt = DateTime.UtcNow
        };

        db.Expenses.Add(expense);
        await db.SaveChangesAsync();

        return Results.Created(
            $"/api/expenses/{expense.Id}",
            expense
        );
    }
);

app.MapPut(
    "/api/expenses/{id:int}",
    async (
        int id,
        ExpenseRequest request,
        DriveMateDbContext db
    ) =>
    {
        var expense = await db.Expenses.FindAsync(id);

        if (expense == null)
        {
            return Results.NotFound();
        }

        if (
            request.VehicleId <= 0 ||
            string.IsNullOrWhiteSpace(request.Category) ||
            request.Amount < 0
        )
        {
            return Results.BadRequest(
                "Invalid expense values."
            );
        }

        expense.VehicleId = request.VehicleId;
        expense.Category = request.Category
            .Trim()
            .ToUpperInvariant();
        expense.Amount = request.Amount;
        expense.Date = request.Date;
        expense.Description = request.Description?.Trim();

        await db.SaveChangesAsync();

        return Results.Ok(expense);
    }
);

app.MapDelete(
    "/api/expenses/{id:int}",
    async (int id, DriveMateDbContext db) =>
    {
        var expense = await db.Expenses.FindAsync(id);

        if (expense == null)
        {
            return Results.NotFound();
        }

        db.Expenses.Remove(expense);
        await db.SaveChangesAsync();

        return Results.NoContent();
    }
);

app.Run();

record WeatherForecast(DateOnly Date, int TemperatureC, string? Summary)
{
    public int TemperatureF => 32 + (int)(TemperatureC / 0.5556);
}
