# Smart-X

Smart-X is a mock hybrid IoT ecosystem that simulates distributed environments that the app monitors and manages. It exists to prove that the data structures behind it can handle large loads with ease.

The system is made up of two projects that communicate through HTTP:

| Project | Folder | What it is | Where it runs |
|---|---|---|---|
| **Smart-X.Api** | `backend/` | ASP.NET Core Web API that receives telemetry, stores sensors, and holds all the data structures | Docker container |
| **Smart-X.Wpf** | `frontend/` | WPF desktop dashboard that pushes and receives data from the API | Locally |

The WPF app is a Windows desktop application and cannot run inside a Docker container, which is why only the API is containerised.

## Part 1

In Part 1, I implemented the initial application: a dashboard to navigate to all required pages when completed and the **Sensor Data Ingestion and Telemetry** page to register sensors, ingest sensor data, and view telemetry. The API processes advanced, object-oriented C# concepts with the telemetry data to do demos (generics, operator overloading, advanced arrays and lists, recursion).

## Part 2

In Part 2, I added the **Real-Time Command Stream and History** page, which has a live dashboard showing incoming sensor data, where users can search sensors by category and manually override sensors. The API processes telemetry through purpose-chosen data structures (queue, priority queue, stack, dictionary, sorted dictionary and hash set).

---

## Prerequisites

Install these before you start:

| Requirement | Why | How to check |
|---|---|---|
| Windows 10 or 11 | The WPF frontend only runs on Windows | Press Win + R, type `winver` and press Enter. It should say Windows 10 or Windows 11. |
| [.NET 10 SDK](https://dotnet.microsoft.com/download) | Restores and runs the frontend | `dotnet --list-sdks` should list `10.0.x` |
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | Builds and runs the API container (backend) | `docker --version` and `docker compose version` |
| VS Code (or Visual Studio 2022 17.12+) | Editing and running terminals | type `code --version` in a terminal (VS Code), or look for Visual Studio in the Start menu |

An internet connection is needed on the first run, because Docker pulls the .NET base images and NuGet downloads packages.

## Setup Step-by-Step

1. Open the project folder (the one containing `Smart-X.sln` and `docker-compose.yml`) in VS Code.
2. Open Docker Desktop and wait until the engine reports that it is running.
3. Open a terminal in the project root and restore the dependencies:

   ```
   dotnet restore
   ```

   This restores NuGet packages for both projects (Swashbuckle for the API; the frontend needs no extra packages).

4. Build and start the API in the background:

   ```
   docker compose up --build -d
   ```

   The `-d` flag matters: without it the terminal stays attached to the container's logs and you would need a second terminal for the next step. The first build takes a few minutes while images download.

5. Confirm the API is alive. Open either of these in Chrome or Edge:

   - http://localhost:8080/swagger : interactive API page listing every endpoint
   - http://localhost:8080/api/health : should return `{"status":"OK", ...}`

6. Start the WPF app:

   ```
   cd frontend
   dotnet run
   ```

7. A window should open with the working application.

### Running it from Visual Studio instead

Open `Smart-X.sln`, right-click the solution, choose **Set Startup Projects**, then **Multiple startup projects**, and set both `Smart-X.Api` and `Smart-X.Wpf` to **Start**. This runs the API directly (not in Docker) on port 5080, so also follow the *Pointing the frontend at a different API address* section below.

## Configuration

### Ports and addresses

| How the API is run | API address | Swagger |
|---|---|---|
| Docker (`docker compose up`) | `http://localhost:8080` | `http://localhost:8080/swagger` |
| `dotnet run` in `backend/` | `http://localhost:5080` (HTTPS: `https://localhost:5081`) | `http://localhost:5080/swagger` |

- Docker's port mapping is set in `docker-compose.yml` (`"8080:8080"`, host port first). To use another host port, change the first number, for example `"9090:8080"`.
- The `dotnet run` ports are set in `backend/Properties/launchSettings.json`.

### Pointing the frontend at a different API address

The frontend defaults to `http://localhost:8080` (the Docker address). If the API is running anywhere else, change the default in `frontend/Services/Smart_XApiClient.cs`:

```csharp
public Smart_XApiClient(string baseUrl = "http://localhost:8080")
```

For example, use `http://localhost:5080` when running the API with `dotnet run`, then rebuild the frontend (stop it and run `dotnet run` again).

### Data and storage

- Sensors, telemetry, alerts, error states and override history are held **in memory**. Restarting or rebuilding the API container resets them, so re-register your sensors after a restart.
- Uploaded hardware logs are the exception: the files and their classifications are saved in a Docker volume named `sensor-logs`, so they survive restarts. Logs belong to a sensor ID, so they reappear once you register a sensor with the same ID again. Deleting a sensor deletes its logs. `docker compose down -v` deletes the volume too.

## Running day to day

| Task | Command (from the project root) |
|---|---|
| Start the API | `docker compose up -d` |
| Rebuild after changing backend code | `docker compose up --build -d` |
| View API logs | `docker compose logs -f` |
| Stop the API (keeps log files) | `docker compose down` |
| Stop the API and delete uploaded logs | `docker compose down -v` |
| Start the frontend | `cd frontend` then `dotnet run` |

Always rebuild with `--build` after backend changes. Docker reuses the old image otherwise, and new endpoints will return `Route not found`.

## App Functions

### As of Part 2

- Register a sensor using a unique ID, a deployment location and a category
- View all sensors in a list
- Edit and delete sensors (the ID cannot be changed once registered)
- Simulate sensor results, including occasional anomalous spikes
- Attach hardware logs to sensors: choose a file, classify it (a classification is suggested automatically), upload it, then view, filter, download or delete it under **Manage Logs**
- View tooltips and field hints
- Toggle guidance using the guidance panel
- Pause and resume the live refresh (every 0.75 seconds) on the **Real-Time Command Stream** page.
- Search for sensors, alerts, and logs by category.
- Receive incoming readings live with anomalous readings highlighted.
- Simulate 3 readings per visible sensor in one batch.
- Watch alerts and statuses react to simulated sensors.
- Manually override sensors with commands below:
  
  | Command | Effect |
  |---|---|
  | Reset | Clears the sensor's active error state |
  | Calibrate | Marks the sensor as calibrating |
  | Disable | The API rejects the sensor's telemetry until it is enabled again |
  | Enable | Allows telemetry again |

- Undo the last override, restoring the sensor's previous state.
- View sensor alerts from most to least severe.
- View override history.

### Hardware logs: classify, upload, download

Select a sensor on the registration page, then use **Attach Hardware Log** or **Manage Logs**.

1. **Choose a file:** **Attach Hardware Log** opens a dialog with a Browse button (a standard file picker). Files are checked before upload: allowed types are `.log .txt .csv .json .xml .cfg .conf .ini .yaml .yml .bin .hex .fw .dat .zip`, up to 10 MB, and not empty.
2. **Classify it:** the API suggests a classification and explains why. Change it if it is wrong. The options are Diagnostic, Error, Calibration, Firmware, Configuration, Raw Data and Other. The suggestion comes from the file name, the extension and the start of the file's contents (for example, a `.log` containing `ERROR` is suggested as Error).
3. **Upload:** the file is stored on the API with its classification.
4. **Download:** **Manage Logs** lists the sensor's logs, newest first. Use the filter to show one classification, then **Download...** to save a copy under its original name, or **Delete** to remove it.

The **Network Topology and Mesh Routing** tile is not built yet and shows a "coming soon" message.

## API endpoints

Browse and try all of them at `/swagger`.

| Method | Route | Purpose |
|---|---|---|
| GET | `/api/health` | Quick check that the API is alive |
| POST | `/api/sensors` | Register a sensor |
| GET | `/api/sensors?category=` | List sensors, optionally filtered by category |
| GET / PUT / DELETE | `/api/sensors/{id}` | Get, edit or delete one sensor |
| POST | `/api/logs/classify` | Suggest a classification from a file name and the start of its contents |
| POST | `/api/sensors/{id}/logs` | Upload a log (multipart: `file`, optional `classification`; omit it to classify automatically) |
| GET | `/api/sensors/{id}/logs?classification=` | List a sensor's logs, optionally filtered by classification |
| GET | `/api/sensors/{id}/logs/{logId}/download` | Download the original file |
| DELETE | `/api/sensors/{id}/logs/{logId}` | Delete one log |
| POST | `/api/telemetry` | Submit one reading (goes through the FIFO queue) |
| POST | `/api/telemetry/batch` | Submit many readings at once |
| GET | `/api/telemetry` and `/api/telemetry/{sensorId}` | Recent readings, newest first |
| GET | `/api/stream/snapshot?category=` | Everything the dashboard shows, in one call |
| POST | `/api/commands/override` | Apply a manual override |
| POST | `/api/commands/undo` | Undo the most recent override |
| GET | `/api/commands/history` | Override history |
| POST | `/api/alerts/acknowledge` | Remove the most severe alert |
| POST | `/api/alerts/clear` | Clear all alerts |

## Troubleshooting

| Symptom | Likely cause and fix |
|---|---|
| Dashboard shows "API unreachable", or the sensor list is empty | The API is not running or the frontend points at the wrong address. Check `docker compose ps`, open `http://localhost:8080/api/health`, and compare with the address in `Smart_XApiClient.cs`. |
| `Route not found` on an endpoint that should exist | Docker is serving an old image. Run `docker compose up --build -d`. |
| `docker compose` fails with a connection or daemon error | Docker Desktop is not running. Start it and wait for the engine to be ready. |
| `Bind for 0.0.0.0:8080 failed: port is already allocated` | Something else is using port 8080. Stop it, or change the host port in `docker-compose.yml` and in `Smart_XApiClient.cs`. |
| `dotnet restore` or `dotnet run` complains about the target framework | The .NET 10 SDK is missing. Check `dotnet --list-sdks`. |
| An upload is refused | The file type is not allowed, the file is empty, or it is over 10 MB. The message says which. |
| The WPF window never appears, or closes straight away | The app now shows an error box and saves details to `%LOCALAPPDATA%\Smart-X\crash.log`. To see the error in the terminal instead, temporarily change `<OutputType>WinExe</OutputType>` to `Exe` in `frontend/Smart-X.Wpf.csproj`, run `dotnet run`, then change it back. |
| Sensors disappear after restarting the API | Expected: storage is in memory. Re-register them. |
| A sensor's telemetry is rejected with a conflict message | The sensor was disabled by a manual override. Apply **Enable** or **Undo Last**. |
| Certificate errors when using the HTTPS address (`dotnet run` only) | Run `dotnet dev-certs https --trust` once, or use the HTTP address. |
| Odd XAML or build errors after pulling changes | Delete the `bin` and `obj` folders in `frontend/` and `backend/`, then run `dotnet restore` again. |
