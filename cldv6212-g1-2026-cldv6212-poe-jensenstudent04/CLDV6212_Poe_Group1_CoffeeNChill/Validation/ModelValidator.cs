using CoffeeNChill.Functions.Models;

namespace CoffeeNChill.Functions.Validation;

// Practice #7: validate everything from the request before it touches the repository.
// Extend this class as new request types are added - don't scatter ad-hoc "if" checks
// inside individual functions.
public class ModelValidator
{
    public IReadOnlyList<string> ValidateMenuItemRequest(MenuItemRequest request, bool requireCategoryAndSku)
    {
        var errors = new List<string>();

        if (requireCategoryAndSku)
        {
            if (string.IsNullOrWhiteSpace(request.Category)) errors.Add("Category is required.");
            if (string.IsNullOrWhiteSpace(request.Sku)) errors.Add("Sku is required.");
        }

        if (string.IsNullOrWhiteSpace(request.Name)) errors.Add("Name is required.");
        if (request.Price is null || request.Price < 0) errors.Add("Price must be zero or greater.");

        return errors;
    }

    // Added for DownloadStaffDocument (Maheshen) - static since it doesn't need any
    // injected dependencies, unlike ValidateMenuItemRequest above.
    public static string ValidateRequired(string value, string fieldName)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            throw new ArgumentException($"{fieldName} is required.");
        }

        return value.Trim();
    }
}
