using CoffeeNChill.Functions.Models;

namespace CoffeeNChill.Functions.Validation;

public class OrderValidator
{
    public IReadOnlyList<string> ValidateOrderRequest(OrderRequest request, bool requireIdAndName)
    {
        var errors = new List<string>();

        if (requireIdAndName)
        {
            if (request.OrderId == null) errors.Add("ID is required.");
            if (string.IsNullOrWhiteSpace(request.CustomerName)) errors.Add("Customer name is required.");
        }

        if (request.SelectedItemSKUs == null) errors.Add("At least 1 SKU must be selected.");
        if (request.TotalPrice is null || request.TotalPrice < 0) errors.Add("Total price must be zero or greater.");

        return errors;
    }

    public static string ValidateRequired(string value, string fieldName)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            throw new ArgumentException($"{fieldName} is required.");
        }

        return value.Trim();
    }
}