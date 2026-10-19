using GadgetList;

namespace GadgetList
{
    public class CustomLinkedList
    {
        private Node head;

        // Add to end
        public void Add(Gadget gadget)
        {
            Node newNode = new Node(gadget);

            if (head == null)
            {
                head = newNode;
                return;
            }

            Node current = head;
            while (current.Next != null)
                current = current.Next;

            current.Next = newNode;
        }

        // Insert at position
        public void InsertAt(int position, Gadget gadget)
        {
            Node newNode = new Node(gadget);

            if (position <= 0 || head == null)
            {
                newNode.Next = head;
                head = newNode;
                return;
            }

            Node current = head;
            int index = 0;

            while (current.Next != null && index < position - 1)
            {
                current = current.Next;
                index++;
            }

            newNode.Next = current.Next;
            current.Next = newNode;
        }

        // Remove by name
        public bool Remove(string name)
        {
            if (head == null)
                return false;

            if (head.Data.Name.Equals(name, StringComparison.OrdinalIgnoreCase))
            {
                head = head.Next;
                return true;
            }

            Node current = head;

            while (current.Next != null)
            {
                if (current.Next.Data.Name.Equals(name, StringComparison.OrdinalIgnoreCase))
                {
                    current.Next = current.Next.Next;
                    return true;
                }

                current = current.Next;
            }

            return false;
        }

        // Find gadget
        public Gadget Find(string name)
        {
            Node current = head;

            while (current != null)
            {
                if (current.Data.Name.Equals(name, StringComparison.OrdinalIgnoreCase))
                    return current.Data;

                current = current.Next;
            }

            return null;
        }

        // Count gadgets
        public int Count()
        {
            int count = 0;
            Node current = head;

            while (current != null)
            {
                count++;
                current = current.Next;
            }

            return count;
        }

        // Reverse list
        public void Reverse()
        {
            Node previous = null;
            Node current = head;

            while (current != null)
            {
                Node next = current.Next;
                current.Next = previous;
                previous = current;
                current = next;
            }

            head = previous;
        }

        // Clear list
        public void Clear()
        {
            head = null;
        }

        // Display all gadgets
        public void Display()
        {
            Console.WriteLine("\nAll cll Gadgets:");

            if (head == null)
            {
                Console.WriteLine("List is empty.");
                return;
            }

            Node current = head;

            while (current != null)
            {
                if (current.Data.Everyday)
                {
                    Console.WriteLine($"{current.Data.Name} - Everyday");
                }
                else
                {
                    Console.WriteLine($"{current.Data.Name} - {string.Join(", ", current.Data.Days)}");
                }

                current = current.Next;
            }
        }

        // Display gadgets for a day
        public void DisplayForDay(DayOfWeek day)
        {
            Console.WriteLine($"\ncll for {day}:");

            Node current = head;

            while (current != null)
            {
                if (current.Data.Everyday || current.Data.Days.Contains(day))
                {
                    Console.WriteLine(current.Data.Name);
                }

                current = current.Next;
            }
        }
    }
}