using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using Microsoft.Win32;
using Smart_X.frontend.Models;

namespace Smart_X.frontend.Views;

public partial class RegisterDevice : Page
{
    private readonly Random _rng = new();
    private List<SensorRecord> _sensors = new();

    // null = registering a new sensor; otherwise holds the ID of the sensor being edited.
    private string? _editingSensorId;

    public RegisterDevice()
    {
        InitializeComponent();
        CategoryBox.ItemsSource = Enum.GetValues(typeof(SensorCategory));
        CategoryBox.SelectedIndex = 0;
    }

    private async void Page_Loaded(object sender, RoutedEventArgs e)
    {
        ApplyGuidanceVisibility();
        await RefreshSensorsAsync();
    }

    private void ApplyGuidanceVisibility()
    {
        var visible = MainWindow.GuidanceEnabled ? Visibility.Visible : Visibility.Collapsed;
        GuidancePanel.Visibility = visible;
        IdGuidanceHint.Visibility = visible;
        LocationGuidanceHint.Visibility = visible;
        CategoryGuidanceHint.Visibility = visible;
    }

    private void GenerateId_Click(object sender, RoutedEventArgs e)
    {
        SensorIdBox.Text = $"SIM-{_rng.Next(1000, 9999)}";
    }

    private async void Refresh_Click(object sender, RoutedEventArgs e) => await RefreshSensorsAsync();

    private async Task RefreshSensorsAsync()
    {
        _sensors = await MainWindow.ApiClient.GetSensorsAsync();
        SensorGrid.ItemsSource = null;
        SensorGrid.ItemsSource = _sensors;
    }

    // Handles both "Register Sensor" (new) and "Save Changes" (editing), depending on _editingSensorId.
    private async void Save_Click(object sender, RoutedEventArgs e)
    {
        if (_editingSensorId is null)
            await RegisterNewSensorAsync();
        else
            await SaveEditedSensorAsync(_editingSensorId);
    }

    private async Task RegisterNewSensorAsync()
    {
        var id = SensorIdBox.Text.Trim();
        var location = LocationBox.Text.Trim();

        if (string.IsNullOrWhiteSpace(id))
        {
            ShowRegistrationStatus("A unique identifier is required before this sensor can be registered.", isError: true);
            SensorIdBox.Focus();
            return;
        }
        if (string.IsNullOrWhiteSpace(location))
        {
            ShowRegistrationStatus("Add a deployment location so the sensor can be found later during troubleshooting.", isError: true);
            LocationBox.Focus();
            return;
        }
        if (_sensors.Any(s => string.Equals(s.Id, id, StringComparison.OrdinalIgnoreCase)))
        {
            ShowRegistrationStatus($"'{id}' is already registered. Choose a different identifier.", isError: true);
            SensorIdBox.Focus();
            return;
        }

        var record = new SensorRecord
        {
            Id = id,
            DeploymentLocation = location,
            Category = (SensorCategory)CategoryBox.SelectedItem,
            IsSimulated = SimulatedCheckBox.IsChecked == true
        };

        var (success, error, _) = await MainWindow.ApiClient.RegisterSensorAsync(record);
        if (success)
        {
            ShowRegistrationStatus($"'{id}' registered successfully.", isError: false);
            ResetForm();
            await RefreshSensorsAsync();
        }
        else
        {
            ShowRegistrationStatus(error ?? "Registration failed for an unknown reason.", isError: true);
        }
    }

    private async Task SaveEditedSensorAsync(string id)
    {
        var location = LocationBox.Text.Trim();

        if (string.IsNullOrWhiteSpace(location))
        {
            ShowRegistrationStatus("Add a deployment location before saving changes.", isError: true);
            LocationBox.Focus();
            return;
        }

        var updated = new SensorRecord
        {
            Id = id,
            DeploymentLocation = location,
            Category = (SensorCategory)CategoryBox.SelectedItem,
            IsSimulated = SimulatedCheckBox.IsChecked == true
        };

        var (success, error, _) = await MainWindow.ApiClient.UpdateSensorAsync(id, updated);
        if (success)
        {
            ShowRegistrationStatus($"'{id}' updated successfully.", isError: false);
            ResetForm();
            await RefreshSensorsAsync();
        }
        else
        {
            ShowRegistrationStatus(error ?? "Update failed for an unknown reason.", isError: true);
        }
    }

    private void EditSelected_Click(object sender, RoutedEventArgs e)
    {
        if (SensorGrid.SelectedItem is not SensorRecord sensor)
        {
            MessageBox.Show("Select a registered sensor first, then click \"Edit Selected\".",
                "No sensor selected", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        _editingSensorId = sensor.Id;

        // ID can't change once registered — lock it and load the rest into the form.
        SensorIdBox.Text = sensor.Id;
        SensorIdBox.IsEnabled = false;
        GenerateIdButton.IsEnabled = false;
        LocationBox.Text = sensor.DeploymentLocation;
        CategoryBox.SelectedItem = sensor.Category;
        SimulatedCheckBox.IsChecked = sensor.IsSimulated;

        FormHeaderText.Text = $"Editing '{sensor.Id}'";
        SaveButton.Content = "Save Changes";
        CancelEditButton.Visibility = Visibility.Visible;

        ShowRegistrationStatus($"Editing '{sensor.Id}'. Update the fields and click Save Changes, or Cancel Edit to discard.", isError: false);
    }

    private void CancelEdit_Click(object sender, RoutedEventArgs e)
    {
        ResetForm();
        RegistrationStatusText.Text = string.Empty;
    }

    private void ResetForm()
    {
        _editingSensorId = null;
        SensorIdBox.Clear();
        SensorIdBox.IsEnabled = true;
        GenerateIdButton.IsEnabled = true;
        LocationBox.Clear();
        CategoryBox.SelectedIndex = 0;
        SimulatedCheckBox.IsChecked = true;

        FormHeaderText.Text = "Register a Sensor";
        SaveButton.Content = "Register Sensor";
        CancelEditButton.Visibility = Visibility.Collapsed;
    }

    private async void DeleteSelected_Click(object sender, RoutedEventArgs e)
    {
        if (SensorGrid.SelectedItem is not SensorRecord sensor)
        {
            MessageBox.Show("Select a registered sensor first, then click \"Delete Selected\".",
                "No sensor selected", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        var confirm = MessageBox.Show(
            $"Delete '{sensor.Id}'? This cannot be undone.",
            "Confirm delete",
            MessageBoxButton.YesNo,
            MessageBoxImage.Warning);

        if (confirm != MessageBoxResult.Yes) return;

        var (success, error) = await MainWindow.ApiClient.DeleteSensorAsync(sensor.Id);
        if (success)
        {
            // If the sensor being deleted was mid-edit, drop back to a clean "register" form.
            if (_editingSensorId == sensor.Id) ResetForm();
            await RefreshSensorsAsync();
        }
        else
        {
            MessageBox.Show(error ?? "The sensor could not be deleted.", "Delete failed",
                MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }

    private void ShowRegistrationStatus(string message, bool isError)
    {
        RegistrationStatusText.Text = message;
        RegistrationStatusText.Foreground = isError
            ? (Brush)FindResource("DangerBrush")
            : (Brush)FindResource("OkBrush");
    }

    private void SensorGrid_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        var selected = SensorGrid.SelectedItem as SensorRecord;
        SelectedSensorHint.Text = selected is null
            ? "Select a sensor above to simulate telemetry, attach a log, edit, or delete it."
            : $"Selected: {selected.Id}";
    }

    private async void SimulateTelemetry_Click(object sender, RoutedEventArgs e)
    {
        if (SensorGrid.SelectedItem is not SensorRecord sensor)
        {
            MessageBox.Show("Select a registered sensor first.", "No sensor selected", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        var isAnomalous = _rng.NextDouble() < 0.2;
        var baseValue = sensor.Category switch
        {
            SensorCategory.Temperature => 21.0,
            SensorCategory.Humidity => 45.0,
            SensorCategory.Pressure => 1013.0,
            SensorCategory.Motion => 0.0,
            SensorCategory.Vibration => 0.2,
            _ => 10.0
        };
        var value = isAnomalous ? baseValue * (1.8 + _rng.NextDouble()) : baseValue + (_rng.NextDouble() * 2 - 1);
        var unit = sensor.Category switch
        {
            SensorCategory.Temperature => "°C",
            SensorCategory.Humidity => "%RH",
            SensorCategory.Pressure => "hPa",
            SensorCategory.Motion => "events",
            SensorCategory.Vibration => "g",
            _ => "units"
        };

        var reading = new TelemetryReading
        {
            SensorId = sensor.Id,
            Value = Math.Round(value, 2),
            Unit = unit,
            IsAnomalous = isAnomalous
        };

        var (success, error) = await MainWindow.ApiClient.SendTelemetryAsync(reading);

        var line = success
            ? $"[{DateTime.Now:HH:mm:ss}] {sensor.Id} → {reading.Value} {reading.Unit}{(isAnomalous ? "  ⚠ ANOMALY" : "")}"
            : $"[{DateTime.Now:HH:mm:ss}] {sensor.Id} → send failed: {error}";

        TelemetryFeed.Items.Insert(0, line);
        while (TelemetryFeed.Items.Count > 50) TelemetryFeed.Items.RemoveAt(TelemetryFeed.Items.Count - 1);

        if (isAnomalous && MainWindow.GuidanceEnabled)
        {
            GuidanceBody.Text = $"⚠ {sensor.Id} just reported a spike ({reading.Value} {reading.Unit}). " +
                                 "Compare it against recent readings in the feed, and consider attaching the sensor's hardware log for this time window to help trace the cause.";
        }

        if (success) await RefreshSensorsAsync();
    }

    private async void AttachLog_Click(object sender, RoutedEventArgs e)
    {
        if (SensorGrid.SelectedItem is not SensorRecord sensor)
        {
            MessageBox.Show("Select a registered sensor first, then attach a log file to its profile.",
                "No sensor selected", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        var dialog = new OpenFileDialog
        {
            Title = $"Attach hardware log to {sensor.Id}",
            Filter = "Log files (*.log;*.txt;*.csv)|*.log;*.txt;*.csv|All files (*.*)|*.*",
            Multiselect = false
        };

        if (dialog.ShowDialog() != true) return;

        var (success, error) = await MainWindow.ApiClient.UploadLogAsync(sensor.Id, dialog.FileName);

        if (success)
        {
            MessageBox.Show($"'{System.IO.Path.GetFileName(dialog.FileName)}' attached to {sensor.Id}.",
                "Log attached", MessageBoxButton.OK, MessageBoxImage.Information);
        }
        else
        {
            MessageBox.Show(error ?? "The log could not be uploaded.", "Upload failed",
                MessageBoxButton.OK, MessageBoxImage.Error);
        }
    }
}
