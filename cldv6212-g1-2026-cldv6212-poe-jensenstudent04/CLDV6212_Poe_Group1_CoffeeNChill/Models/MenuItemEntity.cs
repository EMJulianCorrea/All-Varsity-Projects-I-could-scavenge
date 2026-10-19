using Azure;
using Azure.Data.Tables;

namespace CoffeeNChill.Functions.Models;

/* This is the Table Storage shape - PartitionKey/RowKey/Timestamp/ETag are required by
   ITableEntity. Keep this separate from MenuItem (below) so the rest of the app never has
   to think about Table Storage concepts directly. */
public class MenuItemEntity : ITableEntity
{
    public string PartitionKey { get; set; } = default!; // Category, e.g. "Hot Drinks"
    public string RowKey { get; set; } = default!;        // SKU, e.g. "COF-001"
    public DateTimeOffset? Timestamp { get; set; }
    public ETag ETag { get; set; }

    public string Name { get; set; } = default!;
    public string? Description { get; set; }
    public double Price { get; set; }
    public bool IsAvailable { get; set; }
}
