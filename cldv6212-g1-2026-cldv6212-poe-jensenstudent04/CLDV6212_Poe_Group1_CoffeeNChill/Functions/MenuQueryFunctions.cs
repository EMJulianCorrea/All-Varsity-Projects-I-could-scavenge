using CoffeeNChill.Functions.Helpers;
using CoffeeNChill.Functions.Repository;
using Microsoft.Azure.Functions.Worker;
using Microsoft.Azure.Functions.Worker.Http;
using Microsoft.Extensions.Logging;

namespace CoffeeNChill.Functions.Functions;

/* TODO (Julian): implement these two read endpoints against IMenuRepository.
   IMenuRepository is already registered in Program.cs, so you don't need to touch
   TableClient/TableServiceClient directly - just call the repository methods below and
   return the result with HttpResponseHelper.OkAsync (see MenuFunctions.cs for the pattern). */
public class MenuQueryFunctions
{
    private readonly IMenuRepository _menuRepository;
    private readonly ILogger<MenuQueryFunctions> _logger;

    public MenuQueryFunctions(IMenuRepository menuRepository, ILogger<MenuQueryFunctions> logger)
    {
        _menuRepository = menuRepository;
        _logger = logger;
    }

    [Function("GetAllMenuItems")] 
    public async Task<HttpResponseData> GetAllMenuItems(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "menu")] HttpRequestData req)
    {
        var menuItems = await _menuRepository.GetAllAsync();

        return await HttpResponseHelper.OkAsync(req, menuItems);
    }


    [Function("GetMenuItemsByCategory")]
    public async Task<HttpResponseData> GetMenuItemsByCategory(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", Route = "menu/category/{category}")] HttpRequestData req,
        string category)
    {
        _logger.LogInformation("Fetching items from '{Category}'...", category);

        var menuItems = await _menuRepository.GetByCategoryAsync(category);
        return await HttpResponseHelper.OkAsync(req, menuItems);

        /* 
        A category with zero items shouldn't be an error state - the category itself 
        is already a valid input and simply stating that no items are present is 
        what would be expected by users...
        */
    }
}

