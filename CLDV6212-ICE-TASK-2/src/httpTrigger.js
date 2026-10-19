const { app } = require('@azure/functions');
const { QueueServiceClient } = require('@azure/storage-queue');
const crypto = require('crypto');

app.http('SubmitMessage', {
    methods: ['POST'],
    authLevel: 'function',
    route: 'submit',
    handler: async (request, context) => {
        let body;
        try {
            body = await request.json();
        } catch (err) {
            return { status: 400, jsonBody: { error: 'Request body must be valid JSON' } };
        }

        if (!body || typeof body !== 'object' || Array.isArray(body)) {
            return { status: 400, jsonBody: { error: 'Request body must be a JSON object' } };
        }

        const connectionString = process.env.AzureWebJobsStorage;
        const queueName = process.env.QUEUE_NAME || 'messages-queue';

        const queueServiceClient = QueueServiceClient.fromConnectionString(connectionString);
        const queueClient = queueServiceClient.getQueueClient(queueName);
        await queueClient.createIfNotExists();

        const envelope = {
            id: request.headers.get('x-request-id') || crypto.randomUUID(),
            receivedAt: new Date().toISOString(),
            payload: body
        };

        await queueClient.sendMessage(JSON.stringify(envelope));

        context.log(`Queued message ${envelope.id} onto '${queueName}'`);

        return {
            status: 202,
            jsonBody: { status: 'queued', id: envelope.id }
        };
    }
});