using System.Net;
using Azure;
using CoffeeNChill.Functions.Exceptions;
using CoffeeNChill.Functions.Helpers;
using CoffeeNChill.Functions.Repository;
using CoffeeNChill.Functions.Validation;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Extensions.Logging;

namespace CoffeeNChill.Functions.Functions;

public class DocumentFunctions
{
    private readonly IDocumentRepository _documentRepository;
    private readonly ILogger<DocumentFunctions> _logger;

    public DocumentFunctions(IDocumentRepository documentRepository, ILogger<DocumentFunctions> logger)
    {
        _documentRepository = documentRepository;
        _logger = logger;
    }

    [Function("UploadStaffDocument")]
    public async Task<HttpResponseData> UploadStaffDocument(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "documents/upload")] HttpRequestData req)
    {
        try
        {
            if (!req.Headers.TryGetValues("X-File-Name", out var fileNameValues))
            {
                return await HttpResponseHelper.BadRequestAsync(req, "X-File-Name header is required.");
            }

            string fileName = fileNameValues.FirstOrDefault() ?? string.Empty;

            string contentType = req.Headers.TryGetValues("Content-Type", out var contentTypeValues)
                ? contentTypeValues.FirstOrDefault() ?? "application/octet-stream"
                : "application/octet-stream";

            if (!req.Headers.TryGetValues("Content-Length", out var contentLengthValues))
            {
                return await HttpResponseHelper.BadRequestAsync(req, "Content-Length header is required.");
            }

            if (!long.TryParse(contentLengthValues.FirstOrDefault(), out long fileSize))
            {
                return await HttpResponseHelper.BadRequestAsync(req, "Invalid Content-Length.");
            }

            FileValidator.Validate(fileName, contentType, fileSize);

            await _documentRepository.UploadAsync(fileName, req.Body, contentType);

            _logger.LogInformation("Staff document uploaded successfully: {FileName}", fileName);

            var response = req.CreateResponse(HttpStatusCode.Created);
            await response.WriteAsJsonAsync(new { success = true, message = "Staff document uploaded successfully.", fileName });
            return response;
        }
        catch (ArgumentException ex)
        {
            return await HttpResponseHelper.BadRequestAsync(req, ex.Message);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error uploading staff document.");
            return await HttpResponseHelper.ServerErrorAsync(req, "An error occurred while uploading the document.");
        }
    }

    [Function("ListStaffDocuments")]
    public async Task<HttpResponseData> ListStaffDocuments(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "documents")] HttpRequestData req)
    {
        try
        {
            var documents = await _documentRepository.ListAsync();
            var response = req.CreateResponse(HttpStatusCode.OK);
            await response.WriteAsJsonAsync(documents);
            return response;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error listing staff documents.");
            return await HttpResponseHelper.ServerErrorAsync(req, "An error occurred while retrieving documents.");
        }
    }

    [Function("DownloadStaffDocument")]
    public async Task<HttpResponseData> DownloadStaffDocument(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "documents/download/{fileName}")] HttpRequestData req,
        string fileName)
    {
        try
        {
            fileName = ModelValidator.ValidateRequired(fileName, "File name");

            var result = await _documentRepository.DownloadAsync(fileName);

            var response = req.CreateResponse(HttpStatusCode.OK);
            response.Headers.Add("Content-Type", result.ContentType);
            response.Headers.Add("Content-Disposition", $"attachment; filename=\"{fileName}\"");

            await result.Content.CopyToAsync(response.Body);
            return response;
        }
        catch (DocumentNotFoundException ex)
        {
            return await HttpResponseHelper.NotFoundAsync(req, ex.Message);
        }
        catch (ArgumentException ex)
        {
            return await HttpResponseHelper.BadRequestAsync(req, ex.Message);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error downloading staff document.");
            return await HttpResponseHelper.ServerErrorAsync(req, "An error occurred while downloading the document.");
        }
    }
}
