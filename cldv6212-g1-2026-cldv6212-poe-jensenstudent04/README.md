# CoffeeNChill

![.NET](https://img.shields.io/badge/.NET-8.0-512BD4?logo=dotnet&logoColor=white)
![Azure Functions](https://img.shields.io/badge/Azure_Functions-Isolated_Worker-0062AD?logo=azurefunctions&logoColor=white)
![Azure Table Storage](https://img.shields.io/badge/Azure-Table_Storage-0089D6?logo=microsoftazure&logoColor=white)
![Azure Blob Storage](https://img.shields.io/badge/Azure-Blob_Storage-0089D6?logo=microsoftazure&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)
![Postman](https://img.shields.io/badge/Tested_with-Postman-FF6C37?logo=postman&logoColor=white)


CoffeeNChill is a cloud-enabled microservices system built for a canteen of the same name, to help manage long queues, food and beverages, menus, orders, recipe documents, and promotional photos. This project operates using Azure Storage, Serverless Functions, Docker Containerization, Web APIs, JWT Authentication, and VPS Hosting.

## Part 1: Azure Functions, Tables, Blob Storage and Docker Hub

Part 1 focuses on laying out the cloud foundation of the system, replacing the canteen's paper menus and filing cabinet documents using Azure Table Storage, Azure Blob Storage, and HTTP-triggered Azure Functions. Staff documents originally targeted Azure File Share; this was changed to Azure Blob Storage per the official module addendum, since Azurite does not emulate File storage.

## Architecture Overview

This project follows a single-project, folder-layered architecture for the Azure Functions app (isolated worker model, .NET 8):

- **Functions/** - HTTP-triggered endpoints (MenuFunctions, MenuQueryFunctions, DocumentFunctions)
- **Repository/** - abstracts storage access behind interfaces (IMenuRepository, IDocumentRepository), so functions never call the Azure SDKs directly
- **Models/** - domain models, Table Storage entities, and request/response DTOs, kept separate so the API contract doesn't leak storage details
- **Validation/** - centralizes input validation (ModelValidator, FileValidator)
- **Exceptions/** - custom exceptions mapped to proper HTTP status codes (404, 409, etc.)
- **Helpers/** - shared HTTP response formatting

Menu items are stored in Azure Table Storage (`MenuItems` table, PartitionKey = Category, RowKey = SKU). Staff documents are stored in Azure Blob Storage (`staff-docs` container), streamed directly on upload/download rather than buffered in memory, with PDF-only validation on file type, MIME type, and size.

## Tech Requirements

| Tool | Version | Link |
|---|---|---|
| .NET SDK | 8.0 | [dotnet.microsoft.com/download/dotnet/8.0](https://dotnet.microsoft.com/en-us/download/dotnet/8.0) |
| Azure Functions Core Tools | v4 | [Install guide - Microsoft Learn](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local) |
| Docker Desktop | latest | [docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop/) |
| Postman | latest | [postman.com/downloads](https://www.postman.com/downloads/) |
| Visual Studio 2022 | latest | [visualstudio.microsoft.com](https://visualstudio.microsoft.com/) (or VS Code with the C# Dev Kit extension) |

## Setup Step-By-Step

1. Install the tools listed above
2. Clone the repo and open `CLDV6212_Poe_Group1_CoffeeNChill.slnx`
3. Run Azurite in its own container:
   ```
   docker run -p 10000:10000 -p 10001:10001 -p 10002:10002 mcr.microsoft.com/azure-storage/azurite
   ```
4. `local.settings.json` is already included in the repo and points at Azurite - no manual configuration needed
5. Restore and run: `dotnet restore`, then `func start` (or press F5 in Visual Studio)
6. The host lists all function routes on `http://localhost:7100/api/...` - import the Postman collection from `/docs` to test them

## Docker Commands

Standalone containers only in Part 1 - no Docker Compose orchestration.

**Azurite (storage emulator):**
```
docker run -p 10000:10000 -p 10001:10001 -p 10002:10002 mcr.microsoft.com/azure-storage/azurite
```

**Functions app:**
```
docker build -t st10477718/coffeenchill-functions:v1.0 .
docker run -p 7071:80 st10477718/coffeenchill-functions:v1.0
```

Per the module addendum, storage connection settings are baked into the image via `ENV` instructions rather than passed on the `docker run` command.

**Base URL reference** - which one to use depends on how you're running the app:
- Running locally via `func start` / Visual Studio → `http://localhost:7100/api`
- Running via the Docker container above → `http://localhost:7071/api`

Published images:
- `st10477718/coffeenchill-functions:v1.0`
- `st10477718/coffeenchill-azurite:v1.0`

## Menu Endpoints

| Method | Route |
|---|---|
| POST | `/api/menu` |
| GET | `/api/menu` |
| GET | `/api/menu/category/{category}` |
| PUT | `/api/menu/{category}/{id}` |
| DELETE | `/api/menu/{category}/{id}` |

## Document Endpoints

| Method | Route |
|---|---|
| POST | `/api/documents/upload` |
| GET | `/api/documents` |
| GET | `/api/documents/download/{fileName}` |

## Contributions

- **Jensen** - Project scaffold and folder structure, Menu Table Storage repository, CreateMenuItem/UpdateMenuItem/DeleteMenuItem, dependency injection wiring, integration of all team members' work
- **Julian** - GetAllMenuItems/GetMenuItemsByCategory endpoints, README
- **Maheshen** - Staff document Blob Storage repository and functions (Upload/List/Download), file validation
- **Jordan** - Docker containerization, Azurite setup, Docker Hub publishing

## Postman Collection

The full collection lives in `/docs/CoffeeNChill.postman_collection.json`, organized into Menu and Documents folders, using a `baseUrl` collection variable. Import it into Postman and set `baseUrl` to match whichever base URL you're using (see Docker Commands section above).

## Youtube Link

<!-- Add the unlisted YouTube link here once the video is uploaded -->
### Part 1 : https://youtu.be/ooQP1ORtmnc 
