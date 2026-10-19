namespace CoffeeNChill.Functions.Exceptions;

public class DocumentNotFoundException : Exception
{
    public DocumentNotFoundException(string fileName)
        : base($"Staff document '{fileName}' was not found.")
    {


    }
}
