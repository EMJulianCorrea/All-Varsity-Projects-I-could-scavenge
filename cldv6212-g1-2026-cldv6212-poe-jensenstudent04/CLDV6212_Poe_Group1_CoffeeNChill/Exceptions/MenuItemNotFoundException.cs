namespace CoffeeNChill.Functions.Exceptions;

public class MenuItemNotFoundException : Exception
{
    public MenuItemNotFoundException(string category, string sku)
        : base($"Menu item '{sku}' was not found in category '{category}'.")
    {


    }
}
