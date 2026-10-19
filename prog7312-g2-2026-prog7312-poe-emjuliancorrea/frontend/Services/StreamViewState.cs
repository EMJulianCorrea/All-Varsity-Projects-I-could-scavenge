using Smart_X.frontend.Models;

namespace Smart_X.frontend.Services;

/// <summary>
/// What the Command Stream dashboard remembers between visits. A new CommandStream page is created
/// every time it is opened, so anything that must survive leaving and coming back lives here.
/// It is held in memory only: it resets to Live, with no search, when the app is restarted.
/// </summary>
public static class StreamViewState
{
    /// <summary>True = Live (refreshing), false = Paused (frozen).</summary>
    public static bool Live { get; set; } = true;

    /// <summary>The text in the category search box.</summary>
    public static string SearchText { get; set; } = string.Empty;

    /// <summary>The most recent data the dashboard displayed. Shown again, unchanged, if the page is reopened while paused.</summary>
    public static StreamSnapshot? LastSnapshot { get; private set; }

    public static DateTime LastSnapshotAt { get; private set; }

    public static void Remember(StreamSnapshot snapshot)
    {
        LastSnapshot = snapshot;
        LastSnapshotAt = DateTime.Now;
    }
}
