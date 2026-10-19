namespace CoffeeNChill.Functions.Models;

// Returned by ListStaffDocuments.
public class StaffDocumentResponse
{
    public string FileName { get; set; } = string.Empty;
    public long Size { get; set; }
    public string LastModified { get; set; } = string.Empty;
}
