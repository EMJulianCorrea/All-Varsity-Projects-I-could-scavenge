using CoffeeNChill.Functions.Models;

namespace CoffeeNChill.Functions.Repository;

public interface IMenuRepository
{
    Task<MenuItem> CreateAsync(MenuItemEntity entity, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<MenuItem>> GetAllAsync(CancellationToken cancellationToken = default);
    Task<IReadOnlyList<MenuItem>> GetByCategoryAsync(string category, CancellationToken cancellationToken = default);
    Task<MenuItem> UpdateAsync(string category, string sku, MenuItemRequest changes, CancellationToken cancellationToken = default);
    Task DeleteAsync(string category, string sku, CancellationToken cancellationToken = default);
}
