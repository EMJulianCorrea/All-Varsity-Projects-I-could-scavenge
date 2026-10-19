using DriveMate.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace DriveMate.Api.Data;

public class DriveMateDbContext : DbContext
{
    public DriveMateDbContext(DbContextOptions<DriveMateDbContext> options)
        : base(options)
    {
    }

    public DbSet<CloudTestRecord> CloudTestRecords => Set<CloudTestRecord>();
    public DbSet<Vehicle> Vehicles => Set<Vehicle>();
    public DbSet<MaintenanceRecord> MaintenanceRecords =>
        Set<MaintenanceRecord>();
        public DbSet<FuelEntry> FuelEntries => Set<FuelEntry>();
        public DbSet<Expense> Expenses => Set<Expense>();
}