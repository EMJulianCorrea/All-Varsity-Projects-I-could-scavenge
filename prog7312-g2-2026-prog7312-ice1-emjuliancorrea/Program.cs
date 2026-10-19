using System;
using System.Text;
using System.Collections.Generic;
using GadgetList;

namespace GadgetList
{
    public class Program
    {
        public static void Main()
        {
            CustomLinkedList cll = new CustomLinkedList();

            // Add gadgets
            cll.Add(new Gadget("Smartphone", true));
            cll.Add(new Gadget("Wallet", true));
            cll.Add(new Gadget("Laptop", false,
                new List<DayOfWeek>
                {
                DayOfWeek.Monday,
                DayOfWeek.Tuesday,
                DayOfWeek.Wednesday,
                DayOfWeek.Thursday,
                DayOfWeek.Friday
                }));

            cll.Add(new Gadget("Gym Bag", false,
                new List<DayOfWeek>
                {
                DayOfWeek.Tuesday,
                DayOfWeek.Thursday
                }));

            cll.Display();

            // Insert
            cll.InsertAt(2, new Gadget("Power Bank", true));

            Console.WriteLine("\nInsert Power Bank at position 2:");
            cll.Display();

            // Find
            Console.WriteLine("\nFinding Laptop...");
            Gadget found = cll.Find("Laptop");

            if (found != null)
                Console.WriteLine("Laptop found.");
            else
                Console.WriteLine("Laptop not found.");

            // Count
            Console.WriteLine($"\nTotal Gadgets: {cll.Count()}");

            // Remove
            cll.Remove("Wallet");

            Console.WriteLine("\nRemoving Wallet:");
            cll.Display();

            // Reverse
            cll.Reverse();

            Console.WriteLine("\nReversed list:");
            cll.Display();

            // Display for Monday
            cll.DisplayForDay(DayOfWeek.Monday);

            // Display for Thursday
            cll.DisplayForDay(DayOfWeek.Thursday);

            // Clear
            cll.Clear();

            Console.WriteLine("\nCleared list:");
            cll.Display();

            Console.ReadLine();
        }
    }
}