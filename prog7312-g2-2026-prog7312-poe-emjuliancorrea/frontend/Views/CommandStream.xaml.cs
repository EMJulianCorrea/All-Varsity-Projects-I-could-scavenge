using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Threading;
using Smart_X.frontend.Models;
using Smart_X.frontend.Services;

namespace Smart_X.frontend.Views;

public partial class CommandStream : Page, IGuidanceAware
{
    private static readonly string[] Commands = { "Reset", "Calibrate", "Disable", "Enable" };

    private readonly DispatcherTimer _timer = new() { Interval = TimeSpan.FromMilliseconds(750) };
    private readonly Random _rng = new();

    private bool _polling;
    private bool _suppressSelection;
    private bool _restoringState;   // true while the search text is being put back, so it doesn't trigger a fetch
    private string _categoryFilter = string.Empty;
    private List<SensorRecord> _visibleSensors = new();

    public CommandStream()
    {
        InitializeComponent();

        CommandBox.ItemsSource = Commands;
        CommandBox.SelectedIndex = 0;

        _timer.Tick += async (_, _) => await PollAsync();

        // Put the search text back as it was, without triggering a fetch.
        _restoringState = true;
        SearchBox.Text = StreamViewState.SearchText;
        _restoringState = false;

        ShowLiveState();   // after InitializeComponent, so the Checked handler can use named controls
    }

    private async void Page_Loaded(object sender, RoutedEventArgs e)
    {
        RefreshGuidance();
        ShowLiveState();

        if (StreamViewState.Live)
        {
            _timer.Start();
            await PollAsync();
        }
        else if (StreamViewState.LastSnapshot is { } frozen)
        {
            // Paused: show exactly what was on screen when it was paused. Nothing is fetched, so
            // readings that arrived while the user was away do not appear.
            Render(frozen);
            ShowPausedStatus();
        }
        else
        {
            // Paused before anything had ever loaded, so there is nothing to show yet: fetch once.
            await PollAsync(force: true);
            if (StreamViewState.LastSnapshot is not null) ShowPausedStatus();
        }
    }

    private void Page_Unloaded(object sender, RoutedEventArgs e) => _timer.Stop();

    public void RefreshGuidance()
    {
        var visibility = MainWindow.GuidanceEnabled ? Visibility.Visible : Visibility.Collapsed;
        SearchHintText.Visibility = visibility;
        OverrideHintText.Visibility = visibility;
        AlertsHintText.Visibility = visibility;
    }

    // A paused view never fetches, whoever asks (timer, search, an action, returning to the page).
    // Only force: true overrides that.
    private async Task PollAsync(bool force = false)
    {
        if (!force && !StreamViewState.Live) return;
        if (_polling) return;
        _polling = true;

        try
        {
            string filterUsed;
            do
            {
                filterUsed = _categoryFilter;
                var snapshot = await MainWindow.ApiClient.GetSnapshotAsync(filterUsed);

                // The user may have pressed Pause while this request was in flight: drop the result.
                if (!force && !StreamViewState.Live) return;

                if (snapshot is null)
                {
                    ConnectionText.Text = "⚠ API unreachable";
                    ConnectionText.Foreground = (Brush)FindResource("DangerBrush");
                    return;
                }

                Render(snapshot);
                StreamViewState.Remember(snapshot);
            }
            while (filterUsed != _categoryFilter);
        }
        finally
        {
            _polling = false;
        }
    }

    private void Render(StreamSnapshot snapshot)
    {
        ConnectionText.Text = "● Connected";
        ConnectionText.Foreground = (Brush)FindResource("OkBrush");
        StatsText.Text = $"Queue: {snapshot.QueueDepth}   ·   Processed: {snapshot.TotalProcessed}   ·   Sensors: {snapshot.Sensors.Count} of {snapshot.TotalSensors}";

        var selectedId = (SensorGrid.SelectedItem as SensorRecord)?.Id;
        _suppressSelection = true;
        _visibleSensors = snapshot.Sensors;
        SensorGrid.ItemsSource = snapshot.Sensors;
        if (selectedId is not null)
            SensorGrid.SelectedItem = snapshot.Sensors.FirstOrDefault(s => s.Id == selectedId);
        _suppressSelection = false;
        UpdateSelectedSensorText();

        StreamGrid.ItemsSource = snapshot.Recent;
        AlertList.ItemsSource = snapshot.Alerts;
        ErrorList.ItemsSource = snapshot.ActiveErrors;
        OverrideList.ItemsSource = snapshot.Overrides;
    }

    private void SearchBox_TextChanged(object sender, TextChangedEventArgs e)
    {
        SearchPlaceholder.Visibility = string.IsNullOrEmpty(SearchBox.Text) ? Visibility.Visible : Visibility.Collapsed;
        _categoryFilter = SearchBox.Text.Trim();
        StreamViewState.SearchText = SearchBox.Text;

        if (_restoringState) return;
        _ = PollAsync();
    }

    private void LiveToggle_Changed(object sender, RoutedEventArgs e)
    {
        var live = LiveToggle.IsChecked == true;
        StreamViewState.Live = live;   // remembered for the next time this page is opened
        ApplyLiveUi(live);

        if (!live)
        {
            _timer.Stop();
            if (IsLoaded) ShowPausedStatus();
        }
        else if (IsLoaded)   // before the page has loaded, Page_Loaded starts things
        {
            _timer.Start();
            _ = PollAsync();   // catch up straight away rather than waiting for the next tick
        }
    }

    private const string SearchTip = "Filter every panel by sensor category, e.g. 'temp' matches Temperature.";

    private static string ToggleText(bool live) => live ? "● Live" : "❚❚ Paused";

    // The button label and the search box follow the live/paused state. Search is locked while
    // paused because changing it would need fresh data from the API, which a pause must not fetch.
    private void ApplyLiveUi(bool live)
    {
        LiveToggle.Content = ToggleText(live);
        SearchBox.IsEnabled = live;
        SearchBox.ToolTip = live ? SearchTip : "Search is locked while paused. Press Live to change it.";
    }

    // Puts the toggle into the remembered state. The label is set explicitly (in ApplyLiveUi) because
    // setting IsChecked to the value it already has raises no event, so the handler would not run.
    private void ShowLiveState()
    {
        LiveToggle.IsChecked = StreamViewState.Live;
        ApplyLiveUi(StreamViewState.Live);
    }

    private void ShowPausedStatus()
    {
        ConnectionText.Text = StreamViewState.LastSnapshot is null
            ? "❚❚ Paused"
            : $"❚❚ Paused: showing data from {StreamViewState.LastSnapshotAt:HH:mm:ss}";
        ConnectionText.Foreground = (Brush)FindResource("WarnBrush");
    }

    private async void Burst_Click(object sender, RoutedEventArgs e)
    {
        if (_visibleSensors.Count == 0)
        {
            ShowAction("No sensors to simulate. Register one first, or clear the search filter.", isError: true);
            return;
        }

        var readings = new List<TelemetryReading>();
        foreach (var sensor in _visibleSensors)
            for (var i = 0; i < 3; i++)
                readings.Add(TelemetrySimulator.Generate(sensor, _rng));

        var (success, error, result) = await MainWindow.ApiClient.SendBatchAsync(readings);

        if (!success || result is null)
        {
            ShowAction(error ?? "The burst could not be sent.", isError: true);
            return;
        }

        var message = $"Burst sent: {result.Accepted} accepted, {result.Rejected} rejected.";
        if (result.Errors.Count > 0) message += $" {result.Errors[0]}";
        await FinishActionAsync(message, isError: result.Rejected > 0 && result.Accepted == 0);
    }

    private void SensorGrid_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        if (_suppressSelection) return;
        UpdateSelectedSensorText();
    }

    private void UpdateSelectedSensorText()
    {
        SelectedSensorText.Text = SensorGrid.SelectedItem is SensorRecord sensor
            ? $"Target: {sensor.Id}  ({sensor.DeploymentLocation})"
            : "Select a sensor above.";
    }

    private async void ApplyOverride_Click(object sender, RoutedEventArgs e)
    {
        if (SensorGrid.SelectedItem is not SensorRecord sensor)
        {
            ShowAction("Select a sensor first, then choose a command to apply to it.", isError: true);
            return;
        }

        var command = (string)CommandBox.SelectedItem;

        if (command == "Disable")
        {
            var confirm = MessageBox.Show(
                $"Disable '{sensor.Id}'? The API will reject its telemetry until it is enabled again (or the override is undone).",
                "Confirm override", MessageBoxButton.YesNo, MessageBoxImage.Warning);
            if (confirm != MessageBoxResult.Yes) return;
        }

        var (success, error, _) = await MainWindow.ApiClient.OverrideAsync(sensor.Id, command);
        await FinishActionAsync(success ? $"{command} applied to {sensor.Id}." : error ?? "The override failed.", isError: !success);
    }

    private async void UndoOverride_Click(object sender, RoutedEventArgs e)
    {
        var (success, error, undone) = await MainWindow.ApiClient.UndoOverrideAsync();
        await FinishActionAsync(success && undone is not null
            ? $"Undid {undone.Command} on {undone.SensorId}."
            : error ?? "Nothing to undo.", isError: !success);
    }

    private async void AckAlert_Click(object sender, RoutedEventArgs e)
    {
        var (success, error, alert) = await MainWindow.ApiClient.AcknowledgeAlertAsync();
        await FinishActionAsync(success && alert is not null
            ? $"Acknowledged {alert.SeverityLabel.ToLower()} alert for {alert.SensorId}."
            : error ?? "No outstanding alerts.", isError: false);
    }

    private async void ClearAlerts_Click(object sender, RoutedEventArgs e)
    {
        var (success, error) = await MainWindow.ApiClient.ClearAlertsAsync();
        await FinishActionAsync(success ? "All alerts cleared." : error ?? "Could not clear alerts.", isError: !success);
    }

    // Shows the outcome of something the user did. While paused it deliberately does not refresh,
    // because that would pull in everything that happened since the pause; it says so instead.
    private async Task FinishActionAsync(string message, bool isError)
    {
        if (StreamViewState.Live)
        {
            ShowAction(message, isError);
            await PollAsync();
        }
        else
        {
            ShowAction(isError ? message : $"{message} The view is paused, so press Live to see the change.", isError);
        }
    }

    private void ShowAction(string message, bool isError)
    {
        ActionStatusText.Text = message;
        ActionStatusText.Foreground = (Brush)FindResource(isError ? "DangerBrush" : "OkBrush");
    }
}
