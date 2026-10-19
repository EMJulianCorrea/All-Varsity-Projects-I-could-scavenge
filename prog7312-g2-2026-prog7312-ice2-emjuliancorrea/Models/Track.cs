namespace PlaylistApp.Models
{
    public class Track
    {
        public string Title { get; set; }
        public string Artist { get; set; }
        public string Duration { get; set; } // simple "mm:ss" string; swap for TimeSpan if you like

        public Track(string title, string artist, string duration = "0:00")
        {
            Title = title;
            Artist = artist;
            Duration = duration;
        }

        public override string ToString() => $"{Title} — {Artist} ({Duration})";
    }
}
