const { app } = require('@azure/functions');
const { TableClient } = require('@azure/data-tables');

app.storageQueue('WriteToTable', {
    queueName: process.env.QUEUE_NAME || 'messages-queue',
    connection: 'AzureWebJobsStorage',
    handler: async (queueItem, context) => {
        const message = typeof queueItem === 'string' ? JSON.parse(queueItem) : queueItem;

        const connectionString = process.env.AzureWebJobsStorage;
        const tableName = process.env.TABLE_NAME || 'MessagesTable';

        const tableClient = TableClient.fromConnectionString(connectionString, tableName, {
            allowInsecureConnection: true
        });
        await tableClient.createTable();

        const entity = {
            partitionKey: 'messages',
            rowKey: message.id,
            receivedAt: message.receivedAt,
            payload: JSON.stringify(message.payload)
        };

        await tableClient.createEntity(entity);

        context.log(`Wrote entity ${message.id} to table '${tableName}'`);
    }
});