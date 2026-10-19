using Azure;
using Azure.Data.Tables;
using CoffeeNChill.Functions.Exceptions;
using CoffeeNChill.Functions.Models;
using Microsoft.Extensions.Logging;

namespace CoffeeNChill.Functions.Repository;

public class TableMenuRepository : IMenuRepository
{
    private const string TableName = "MenuItems";
    private readonly TableClient _tableClient;
    private readonly ILogger<TableMenuRepository> _logger;

    public TableMenuRepository(TableServiceClient serviceClient, ILogger<TableMenuRepository> logger)
    {
        _logger = logger;
        _tableClient = serviceClient.GetTableClient(TableName);
        _tableClient.CreateIfNotExists();
    }

    public async Task<MenuItem> CreateAsync(MenuItemEntity entity, CancellationToken cancellationToken = default)
    {
        try
        {
            await _tableClient.AddEntityAsync(entity, cancellationToken);
            _logger.LogInformation("Created menu item {Sku} in category {Category}", entity.RowKey, entity.PartitionKey);
            return MenuItem.FromEntity(entity);
        }
        catch (RequestFailedException ex) when (ex.Status == 409)
        {
            _logger.LogWarning("Attempted to create duplicate menu item {Sku} in {Category}", entity.RowKey, entity.PartitionKey);
            throw new InvalidOperationException($"Menu item '{entity.RowKey}' already exists in category '{entity.PartitionKey}'.");
        }
    }

    public async Task<IReadOnlyList<MenuItem>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        var results = new List<MenuItem>();
        await foreach (var entity in _tableClient.QueryAsync<MenuItemEntity>(cancellationToken: cancellationToken))
        {
            results.Add(MenuItem.FromEntity(entity));
        }
        return results;
    }

    public async Task<IReadOnlyList<MenuItem>> GetByCategoryAsync(string category, CancellationToken cancellationToken = default)
    {
        var results = new List<MenuItem>();
        await foreach (var entity in _tableClient.QueryAsync<MenuItemEntity>(
            filter: $"PartitionKey eq '{category}'", cancellationToken: cancellationToken))
        {
            results.Add(MenuItem.FromEntity(entity));
        }
        return results;
    }

    public async Task<MenuItem> UpdateAsync(string category, string sku, MenuItemRequest changes, CancellationToken cancellationToken = default)
    {
        MenuItemEntity existing;
        try
        {
            var response = await _tableClient.GetEntityAsync<MenuItemEntity>(category, sku, cancellationToken: cancellationToken);
            existing = response.Value;
        }
        catch (RequestFailedException ex) when (ex.Status == 404)
        {
            throw new MenuItemNotFoundException(category, sku);
        }

        if (changes.Name is not null) existing.Name = changes.Name;
        if (changes.Description is not null) existing.Description = changes.Description;
        if (changes.Price is not null) existing.Price = changes.Price.Value;
        if (changes.IsAvailable is not null) existing.IsAvailable = changes.IsAvailable.Value;

        await _tableClient.UpdateEntityAsync(existing, existing.ETag, TableUpdateMode.Replace, cancellationToken);
        return MenuItem.FromEntity(existing);
    }

    public async Task DeleteAsync(string category, string sku, CancellationToken cancellationToken = default)
    {
        try
        {
            await _tableClient.DeleteEntityAsync(category, sku, ETag.All, cancellationToken);
        }
        catch (RequestFailedException ex) when (ex.Status == 404)
        {
            throw new MenuItemNotFoundException(category, sku);
        }
    }
}