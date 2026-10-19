namespace CoffeeNChill.Functions.Models;

/* The shape returned to API callers - deliberately does not expose PartitionKey/RowKey/
   ETag, so callers don't need to know this is backed by Table Storage. */
public class MenuItem
{
    public string Category { get; set; } = default!;
    public string Sku { get; set; } = default!;
    public string Name { get; set; } = default!;
    public string? Description { get; set; }
    public double Price { get; set; }
    public bool IsAvailable { get; set; }

    public static MenuItem FromEntity(MenuItemEntity entity) => new()
    {
        Category = entity.PartitionKey,
        Sku = entity.RowKey,
        Name = entity.Name,
        Description = entity.Description,
        Price = entity.Price,
        IsAvailable = entity.IsAvailable
    };
}
