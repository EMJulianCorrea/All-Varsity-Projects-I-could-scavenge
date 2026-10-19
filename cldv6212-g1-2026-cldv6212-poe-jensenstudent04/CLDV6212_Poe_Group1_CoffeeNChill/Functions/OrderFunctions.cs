using System.Net;
using CoffeeNChill.Functions.Exceptions;
using CoffeeNChill.Functions.Helpers;
using CoffeeNChill.Functions.Models;
using CoffeeNChill.Functions.Repository;
using CoffeeNChill.Functions.Validation;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Extensions.Logging;

namespace CoffeeNChill.Functions.Functions;
public class OrderFunctions
{
    private readonly IQueueRepository _queueRepository;
    private readonly OrderValidator _validator;
    private readonly ILogger<OrderFunctions> _logger;

    public OrderFunctions(IQueueRepository queueRepository, OrderValidator validator, ILogger<OrderFunctions> logger)
    {
        _queueRepository = queueRepository;
        _validator = validator;
        _logger = logger;
    }

    [Function("PlaceOrder")]
    public async Task<HttpResponseData> PlaceOrder(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "orders/queue")] HttpRequestData req)
    {
        var request = await req.ReadFromJsonAsync<OrderRequest>();
        if (request is null)
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.BadRequest, "Request body could not be read.");

        var errors = _validator.ValidateOrderRequest(request, requireIdAndName: true);
        if (errors.Count > 0)
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.BadRequest, "Validation failed.", errors);

        var entity = request.ToOrder(request.OrderId!, request.CustomerName!);

        try
        {
            var sent = await _queueRepository.SendMessageAsync(entity);
            _logger.LogInformation("SendOrder succeeded for {CustomerName}", entity.CustomerName);
            return await HttpResponseHelper.OkAsync(req, sent, HttpStatusCode.Created);
        }
        catch (InvalidOperationException ex)
        {
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.Conflict, ex.Message);
        }
    }
}
