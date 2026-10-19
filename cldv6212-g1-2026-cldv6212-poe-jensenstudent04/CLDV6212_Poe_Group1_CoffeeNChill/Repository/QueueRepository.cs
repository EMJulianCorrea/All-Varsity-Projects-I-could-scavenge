using System.Text.Json;
using Azure.Storage.Queues;
using CoffeeNChill.Functions.Models;

namespace CoffeeNChill.Functions.Repository;

public class QueueRepository : IQueueRepository
{
    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);

    private readonly QueueClient _queueClient;
    private bool _queueEnsured;

    public QueueRepository(QueueClient queueClient)
    {
        _queueClient = queueClient;
    }

    public async Task<QueueClient> GetQueueClientAsync(CancellationToken cancellationToken = default)
    {
        if (!_queueEnsured)
        {
            await _queueClient.CreateIfNotExistsAsync(cancellationToken: cancellationToken);
            _queueEnsured = true;
        }

        return _queueClient;
    }

    public string SerializeOrder(Order order) => JsonSerializer.Serialize(order, JsonOptions);

    public async Task<string> SendMessageAsync(Order order, CancellationToken cancellationToken = default)
    {
        var queueClient = await GetQueueClientAsync(cancellationToken);
        var response = await queueClient.SendMessageAsync(SerializeOrder(order), cancellationToken);
        return response.Value.MessageId;
    }

}