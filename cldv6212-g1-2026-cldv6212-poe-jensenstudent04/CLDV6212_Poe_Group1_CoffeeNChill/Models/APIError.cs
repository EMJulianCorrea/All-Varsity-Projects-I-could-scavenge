namespace CoffeeNChill.Functions.Models;

public class APIError
{
    public string Message { get; set; } = default!;
    public IEnumerable<string>? Details { get; set; }

    public APIError(string message, IEnumerable<string>? details = null)
    {
        Message = message;
        Details = details;
    }
}
