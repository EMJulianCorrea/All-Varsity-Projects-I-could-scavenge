namespace CoffeeNChill.Functions.Models;

public class OrderRequest
{
    public string? OrderId { get; set; }
    public string? CustomerName { get; set; }
    public string[]? SelectedItemSKUs { get; set; }
    public double? TotalPrice { get; set; }
    public string? OrderTimestamp { get; set; }

    public Order ToOrder(string orderId, string name) => new()
    {
        OrderId = OrderId ?? Guid.NewGuid().ToString(),
        CustomerName = CustomerName ?? "Unknown",
        SelectedItemSKUs = SelectedItemSKUs ?? Array.Empty<string>(),
        TotalPrice = TotalPrice ?? 0,
        OrderTimestamp = OrderTimestamp ?? DateTimeOffset.UtcNow.ToString("O")
    };
}