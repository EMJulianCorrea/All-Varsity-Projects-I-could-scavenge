using Azure;
using Azure.Data.Tables;

namespace CoffeeNChill.Functions.Models;

/* Table Storage shape for Orders. PartitionKey = OrderDate (yyyy-MM-dd), RowKey = OrderId,
   per the brief. Table Storage has no array type, so SKUs are stored as a comma-separated
   string and split/joined at the Order <-> OrderEntity boundary. */
public class OrderEntity : ITableEntity
{
    public string PartitionKey { get; set; } = default!; // OrderDate
    public string RowKey { get; set; } = default!;        // OrderId
    public DateTimeOffset? Timestamp { get; set; }
    public ETag ETag { get; set; }

    public string CustomerName { get; set; } = default!;
    public string SelectedItemSKUs { get; set; } = default!; // comma-separated
    public double TotalPrice { get; set; }
    public string OrderTimestamp { get; set; } = default!;
    public string Status { get; set; } = "Received";
}