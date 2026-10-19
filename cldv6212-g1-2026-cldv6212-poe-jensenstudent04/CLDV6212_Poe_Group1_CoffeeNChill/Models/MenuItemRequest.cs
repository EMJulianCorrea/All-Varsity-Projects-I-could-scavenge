namespace CoffeeNChill.Functions.Models;

/* The shape accepted from POST/PUT bodies. Nullable so ModelValidator can tell the
   difference between "not provided" and "provided as empty/zero" during updates. */
public class MenuItemRequest
{
    public string? Category { get; set; }
    public string? Sku { get; set; }
    public string? Name { get; set; }
    public string? Description { get; set; }
    public double? Price { get; set; }
    public bool? IsAvailable { get; set; }

    public MenuItemEntity ToEntity(string category, string sku) => new()
    {
        PartitionKey = category,
        RowKey = sku,
        Name = Name ?? string.Empty,
        Description = Description,
        Price = Price ?? 0,
        IsAvailable = IsAvailable ?? true
    };
}
