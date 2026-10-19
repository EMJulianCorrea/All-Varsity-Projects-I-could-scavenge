using System.Windows;
using System.Windows.Input;
using PlaylistApp.Models;

namespace PlaylistApp
{
    public partial class MainWindow : Window
    {
        // The doubly linked list is the single source of truth for playlist
        // order and playback position. The ListBox just mirrors it visually.
        private readonly DoublyLinkedList<Track> _playlist = new();

        public MainWindow()
        {
            InitializeComponent();
            SeedSampleTracks();
        }

        private void SeedSampleTracks()
        {
            AddTrack(new Track("Midnight City", "M83", "4:03"));
            AddTrack(new Track("Everlong", "Foo Fighters", "4:10"));
            AddTrack(new Track("Blinding Lights", "The Weeknd", "3:20"));

            UpdateNowPlaying(_playlist.Current);
        }

        private void AddTrack(Track track)
        {
            _playlist.AddLast(track);
            PlaylistBox.Items.Add(track);
        }

        private void AddButton_Click(object sender, RoutedEventArgs e)
        {
            var title = TitleBox.Text.Trim();
            var artist = ArtistBox.Text.Trim();

            if (string.IsNullOrEmpty(title))
            {
                MessageBox.Show("Give the track a title first.", "Missing title",
                    MessageBoxButton.OK, MessageBoxImage.Information);
                return;
            }

            var wasEmpty = _playlist.Count == 0;
            var track = new Track(title, string.IsNullOrEmpty(artist) ? "Unknown Artist" : artist);
            AddTrack(track);

            if (wasEmpty)
                UpdateNowPlaying(_playlist.Current);

            TitleBox.Clear();
            ArtistBox.Clear();
            TitleBox.Focus();
        }

        private void RemoveButton_Click(object sender, RoutedEventArgs e)
        {
            if (PlaylistBox.SelectedItem is not Track selected)
            {
                MessageBox.Show("Select a track to remove first.", "Nothing selected",
                    MessageBoxButton.OK, MessageBoxImage.Information);
                return;
            }

            _playlist.Remove(selected);
            PlaylistBox.Items.Remove(selected);

            UpdateNowPlaying(_playlist.Current);
        }

        private void NextButton_Click(object sender, RoutedEventArgs e)
        {
            UpdateNowPlaying(_playlist.MoveNext());
        }

        private void PrevButton_Click(object sender, RoutedEventArgs e)
        {
            UpdateNowPlaying(_playlist.MovePrevious());
        }

        private void ShuffleButton_Click(object sender, RoutedEventArgs e)
        {
            var currentlyPlaying = _playlist.Current?.Value;

            _playlist.Shuffle();


            PlaylistBox.Items.Clear();
            foreach (var track in _playlist)
                PlaylistBox.Items.Add(track);

            if (currentlyPlaying != null)
            {
                var node = _playlist.Find(currentlyPlaying);
                if (node != null) _playlist.SetCurrent(node);
            }

            UpdateNowPlaying(_playlist.Current);
        }

        private void PlaylistBox_MouseDoubleClick(object sender, MouseButtonEventArgs e)
        {
            if (PlaylistBox.SelectedItem is not Track selected) return;

            var node = _playlist.Find(selected);
            if (node == null) return;

            _playlist.SetCurrent(node);
            UpdateNowPlaying(node);
        }

        private void UpdateNowPlaying(Node<Track> node)
        {
            if (node == null)
            {
                NowPlayingText.Text = "Scanning...";
                PlaylistBox.SelectedItem = null;
                return;
            }

            NowPlayingText.Text = node.Value.ToString();
            PlaylistBox.SelectedItem = node.Value;
            PlaylistBox.ScrollIntoView(node.Value);
        }
    }
}
