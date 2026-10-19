using Azure;
using Azure.Storage.Blobs;
using Azure.Storage.Blobs.Models;
using CoffeeNChill.Functions.Exceptions;
using CoffeeNChill.Functions.Models;
using Microsoft.Extensions.Logging;

namespace CoffeeNChill.Functions.Repository;

public class BlobDocumentRepository : IDocumentRepository
{
    private readonly BlobContainerClient _containerClient;
    private readonly ILogger<BlobDocumentRepository> _logger;

    public BlobDocumentRepository(BlobServiceClient blobServiceClient, ILogger<BlobDocumentRepository> logger)
    {
        _logger = logger;
        _containerClient = blobServiceClient.GetBlobContainerClient("staff-docs");
        _containerClient.CreateIfNotExists();
    }

    public async Task UploadAsync(string fileName, Stream fileStream, string contentType, CancellationToken cancellationToken = default)
    {
        BlobClient blobClient = _containerClient.GetBlobClient(fileName);

        _logger.LogInformation("Uploading staff document: {FileName}", fileName);

        var options = new BlobUploadOptions
        {
            HttpHeaders = new BlobHttpHeaders { ContentType = contentType }
        };

        await blobClient.UploadAsync(fileStream, options, cancellationToken);
    }

    public async Task<List<StaffDocumentResponse>> ListAsync(CancellationToken cancellationToken = default)
    {
        var documents = new List<StaffDocumentResponse>();

        await foreach (BlobItem blob in _containerClient.GetBlobsAsync(cancellationToken: cancellationToken))
        {
            documents.Add(new StaffDocumentResponse
            {
                FileName = blob.Name,
                Size = blob.Properties.ContentLength ?? 0,
                LastModified = blob.Properties.LastModified?.ToString("O") ?? string.Empty
            });
        }

        return documents;
    }

    public async Task<(Stream Content, string ContentType)> DownloadAsync(string fileName, CancellationToken cancellationToken = default)
    {
        BlobClient blobClient = _containerClient.GetBlobClient(fileName);

        try
        {
            BlobDownloadStreamingResult result = await blobClient.DownloadStreamingAsync(cancellationToken: cancellationToken);
            string contentType = result.Details.ContentType ?? "application/octet-stream";
            return (result.Content, contentType);
        }
        catch (RequestFailedException ex) when (ex.Status == 404)
        {
            _logger.LogWarning("Staff document not found: {FileName}", fileName);
            throw new DocumentNotFoundException(fileName);
        }
    }
}
