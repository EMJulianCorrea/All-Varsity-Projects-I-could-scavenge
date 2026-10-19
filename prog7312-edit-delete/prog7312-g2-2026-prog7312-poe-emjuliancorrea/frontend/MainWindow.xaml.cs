using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Smart_X.frontend.Services;
using Smart_X.frontend.Views;

namespace Smart_X.frontend;

/// <summary>
/// Interaction logic for MainWindow.xaml
/// </summary>
public partial class MainWindow : Window
{
    public static bool GuidanceEnabled = true;
    public static Smart_XApiClient ApiClient { get; } = new Smart_XApiClient();

    public MainWindow()
    {
        InitializeComponent();
        MainFrame.Navigated += MainFrame_Navigated;
        MainFrame.Navigate(new LandingPage(this));
    }
    private void MainFrame_Navigated(object sender, NavigationEventArgs e)
    {
        BackButton.Visibility = MainFrame.CanGoBack ? Visibility.Visible : Visibility.Collapsed;

        BreadcrumbText.Text = e.Content switch
        {
            LandingPage => "",
            RegisterDevice => "/  Sensor Data Ingestion and Telemetry",
            _ => ""
        };
    }
    private void BackButton_Click(object sender, RoutedEventArgs e)
    {
        if (MainFrame.CanGoBack) MainFrame.GoBack();
    }

    private void GuidanceToggle_Changed(object sender, RoutedEventArgs e)
    {
        GuidanceEnabled = GuidanceToggle.IsChecked == true;
        GuidanceToggle.Content = GuidanceEnabled ? "Guidance: On" : "Guidance: Off";
    }

    public void NavigateTo(Page page) => MainFrame.Navigate(page);
}