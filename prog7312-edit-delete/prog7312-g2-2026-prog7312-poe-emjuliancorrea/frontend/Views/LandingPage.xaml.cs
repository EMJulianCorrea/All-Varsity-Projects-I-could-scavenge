using System.Windows;
using System.Windows.Controls;

namespace Smart_X.frontend.Views;

public partial class LandingPage : Page
{
    private readonly MainWindow _owner;

    public LandingPage(MainWindow owner)
    {
        InitializeComponent();
        _owner = owner;
    }

    private void SensorTile_Click(object sender, RoutedEventArgs e)
    {
        _owner.NavigateTo(new RegisterDevice());
    }

    // "Real-Time Command Stream and History" and "Network Topology and Mesh Routing"
    // intentionally do not navigate anywhere yet — this is the in-app guidance
    // that tells the user why, instead of the button silently doing nothing.
    private void UnderContructionTile_Click(object sender, RoutedEventArgs e)
    {
        var tileName = (sender as Button)?.Name switch
        {
            "CommandStreamTile" => "Real-Time Command Stream and History",
            "NetworkTopologyTile" => "Network Topology and Mesh Routing",
            _ => "This section"
        };

        MessageBox.Show(
            $"{tileName} is on the roadmap and isn't wired up yet.\n\nFor now, try \"Sensor Data Ingestion and Telemetry\" to register sensors and simulate readings.",
            "Coming soon",
            MessageBoxButton.OK,
            MessageBoxImage.Information);
    }
}