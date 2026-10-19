using Azure.Storage.Queues;
using CoffeeNChill.Functions.Models;

namespace CoffeeNChill.Functions.Repository;

public interface IQueueRepository
{
    Task<QueueClient> GetQueueClientAsync(CancellationToken cancellationToken = default);
    string SerializeOrder(Order order);
    Task<string> SendMessageAsync(Order order, CancellationToken cancellationToken = default);
}