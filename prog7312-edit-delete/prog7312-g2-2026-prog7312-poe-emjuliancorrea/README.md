# Smart-X — IoT Telemetry Console

## What changed and why

The original repo had **one project** (`Smart-X.csproj`) trying to be both the
WPF desktop app and the ASP.NET Core API. That's why things broke in confusing
ways:

- WPF auto-generates a `Main()` from `App.xaml`'s `StartupUri`. The API's
  `ApiProgram.cs` (top-level statements) generates a *second* `Main()`. Two
  entry points in one assembly causes inconsistent build behavior — including
  `WebApplication` appearing to "not exist" depending on what got compiled.
- `App.xaml`'s `<Application.Resources>` was empty, but every screen
  (`MainWindow`, `LandingPage`, `RegisterDevice`) references styles/brushes
  like `SurfaceBrush`, `PrimaryButtonStyle`, `CardBrush` that were never
  defined anywhere. This would throw a resource-not-found exception the
  moment the app ran.
- There was no way to *view* anything in a browser — the API only returned
  raw JSON with no page to load.

This version splits it into **two real, separate projects**, restores the
missing styles, adds Swagger so there's an actual page to open in Chrome/Edge,
and adds a working Dockerfile for the API.

```
Smart-X.sln
backend/                  ← ASP.NET Core Web API (dockerized)
  Smart-X.Api.csproj
  Program.cs
  Models/SensorRecord.cs
  Dockerfile
  Properties/launchSettings.json
frontend/                 ← WPF desktop app (Windows only, NOT dockerized)
  Smart-X.Wpf.csproj
  App.xaml               ← now contains the full style/brush resource dictionary
  MainWindow.xaml(.cs)
  Views/LandingPage.xaml(.cs)
  Views/RegisterDevice.xaml(.cs)
  Models/SensorModel.cs
  Services/Smart_XApiClient.cs
docker-compose.yml
```

A WPF app is a Windows desktop technology — it cannot run inside a Linux
container, which is what Docker gives you by default. So only the **backend**
is containerized. The frontend still runs natively on Windows and talks to
the API over HTTP, whether that API is running via `dotnet run` or inside
Docker.

## Running the backend and viewing it in Chrome/Edge

**Option A — plain `dotnet run` (no Docker):**
```
cd backend
dotnet run
```
This opens your browser automatically to `http://localhost:5080/swagger` —
an interactive Swagger UI page where you can see and call every endpoint
directly from Chrome/Edge, no separate client needed. Visiting the bare root
(`http://localhost:5080/`) also redirects to `/swagger` automatically now.

**Option B — Docker:**
```
docker compose up --build
```
Then open **http://localhost:8080/swagger** in Chrome or Edge. This uses
plain HTTP inside the container specifically so you don't have to deal with
trusting a self-signed dev certificate just to view it.

Either way, `/api/health` is a quick way to confirm it's alive:
`http://localhost:5080/api/health` or `http://localhost:8080/api/health`.

## Running the WPF frontend

```
cd frontend
dotnet run
```
(or set `Smart-X.Wpf` as the startup project in Visual Studio)

By default `Smart_XApiClient` points at `http://localhost:8080` — matching
the Docker setup. If you're running the backend via `dotnet run` instead
(Option A above, port 5080), update the default in
`frontend/Services/Smart_XApiClient.cs`:

```csharp
public Smart_XApiClient(string baseUrl = "http://localhost:5080")
```

## Other fixes made along the way

- Fixed an inverted visibility check in `RegisterDevice.xaml.cs` —
  `ApplyGuidanceVisibility()` was hiding the guidance panel when guidance was
  turned *on*.
- Moved `AssemblyInfo.cs` into `frontend/Properties/`, since its
  `ThemeInfo` attribute is WPF-specific and doesn't belong in the API project.

## Running both together in Visual Studio

Right-click the **solution** → *Set Startup Projects* → *Multiple startup
projects* → set both `Smart-X.Api` and `Smart-X.Wpf` to **Start**. This
launches the API (with Swagger opening in your browser) and the WPF app
together.
