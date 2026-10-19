namespace GadgetList
{
public class Gadget
{
    public string Name { get; set; }
    public bool Everyday { get; set; }
    public List<DayOfWeek> Days { get; set; }

    public Gadget(string name, bool everyday, List<DayOfWeek> days = null)
    {
        Name = name;
        Everyday = everyday;
        Days = days ?? new List<DayOfWeek>();
    }
}
}