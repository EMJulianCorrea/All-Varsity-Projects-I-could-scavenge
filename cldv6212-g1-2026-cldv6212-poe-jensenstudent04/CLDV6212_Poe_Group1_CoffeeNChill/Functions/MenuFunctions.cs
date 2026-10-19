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

/* Julian's GetAllMenuItems / GetMenuItemsByCategory live in Functions/MenuQueryFunctions.cs
   so both files can be edited in parallel without merge conflicts. */
public class MenuFunctions
{
    private readonly IMenuRepository _menuRepository;
    private readonly ModelValidator _validator;
    private readonly ILogger<MenuFunctions> _logger;

    public MenuFunctions(IMenuRepository menuRepository, ModelValidator validator, ILogger<MenuFunctions> logger)
    {
        _menuRepository = menuRepository;
        _validator = validator;
        _logger = logger;
    }

    [Function("CreateMenuItem")]
    public async Task<HttpResponseData> CreateMenuItem(
        [HttpTrigger(AuthorizationLevel.Anonymous, "post", Route = "menu")] HttpRequestData req)
    {
        var request = await req.ReadFromJsonAsync<MenuItemRequest>();
        if (request is null)
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.BadRequest, "Request body could not be read.");

        var errors = _validator.ValidateMenuItemRequest(request, requireCategoryAndSku: true);
        if (errors.Count > 0)
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.BadRequest, "Validation failed.", errors);

        var entity = request.ToEntity(request.Category!, request.Sku!);

        try
        {
            var created = await _menuRepository.CreateAsync(entity);
            _logger.LogInformation("CreateMenuItem succeeded for {Sku}", entity.RowKey);
            return await HttpResponseHelper.OkAsync(req, created, HttpStatusCode.Created);
        }
        catch (InvalidOperationException ex)
        {
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.Conflict, ex.Message);
        }
    }

    [Function("UpdateMenuItem")]
    public async Task<HttpResponseData> UpdateMenuItem(
        [HttpTrigger(AuthorizationLevel.Anonymous, "put", Route = "menu/{category}/{id}")] HttpRequestData req,
        string category, string id)
    {
        var request = await req.ReadFromJsonAsync<MenuItemRequest>();
        if (request is null)
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.BadRequest, "Request body could not be read.");

        var errors = _validator.ValidateMenuItemRequest(request, requireCategoryAndSku: false);
        if (errors.Count > 0)
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.BadRequest, "Validation failed.", errors);

        try
        {
            var updated = await _menuRepository.UpdateAsync(category, id, request);
            _logger.LogInformation("UpdateMenuItem succeeded for {Sku}", id);
            return await HttpResponseHelper.OkAsync(req, updated);
        }
        catch (MenuItemNotFoundException ex)
        {
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.NotFound, ex.Message);
        }
    }

    [Function("DeleteMenuItem")]
    public async Task<HttpResponseData> DeleteMenuItem(
        [HttpTrigger(AuthorizationLevel.Anonymous, "delete", Route = "menu/{category}/{id}")] HttpRequestData req,
        string category, string id)
    {
        try
        {
            await _menuRepository.DeleteAsync(category, id);
            _logger.LogInformation("DeleteMenuItem succeeded for {Sku}", id);
            return req.CreateResponse(HttpStatusCode.NoContent);
        }
        catch (MenuItemNotFoundException ex)
        {
            return await HttpResponseHelper.ErrorAsync(req, HttpStatusCode.NotFound, ex.Message);
        }
    }
}
