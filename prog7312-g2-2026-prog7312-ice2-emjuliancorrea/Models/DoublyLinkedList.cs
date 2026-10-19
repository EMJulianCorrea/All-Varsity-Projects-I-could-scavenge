using System;
using System.Collections;
using System.Collections.Generic;

namespace PlaylistApp.Models
{
    /// <summary>
    /// A single node in the doubly linked list.
    /// </summary>
    public class Node<T>
    {
        public T Value { get; set; }
        public Node<T> Next { get; set; }
        public Node<T> Previous { get; set; }

        public Node(T value)
        {
            Value = value;
        }
    }

    /// <summary>
    /// Generic doubly linked list with a "Current" pointer, which is what
    /// makes it useful for a playlist: Next()/Previous() just walk the
    /// pointer forwards/backwards instead of doing index math.
    /// </summary>
    public class DoublyLinkedList<T> : IEnumerable<T>
    {
        public Node<T> Head { get; private set; }
        public Node<T> Tail { get; private set; }
        public Node<T> Current { get; private set; }
        public int Count { get; private set; }

        /// <summary>Adds a value to the end of the list.</summary>
        public Node<T> AddLast(T value)
        {
            var node = new Node<T>(value);

            if (Head == null)
            {
                Head = Tail = node;
                Current = node; // first track added becomes current by default
            }
            else
            {
                node.Previous = Tail;
                Tail.Next = node;
                Tail = node;
            }

            Count++;
            return node;
        }

        /// <summary>Finds the first node containing the given value.</summary>
        public Node<T> Find(T value)
        {
            var node = Head;
            var comparer = EqualityComparer<T>.Default;
            while (node != null)
            {
                if (comparer.Equals(node.Value, value))
                    return node;
                node = node.Next;
            }
            return null;
        }

        /// <summary>Removes a specific node, relinking its neighbours.</summary>
        public void Remove(Node<T> node)
        {
            if (node == null) return;

            if (node.Previous != null)
                node.Previous.Next = node.Next;
            else
                Head = node.Next; // was the head

            if (node.Next != null)
                node.Next.Previous = node.Previous;
            else
                Tail = node.Previous; // was the tail

            // If we removed the node the "playhead" was on, move it sensibly.
            if (Current == node)
                Current = node.Next ?? node.Previous;

            node.Next = null;
            node.Previous = null;
            Count--;
        }

        public bool Remove(T value)
        {
            var node = Find(value);
            if (node == null) return false;
            Remove(node);
            return true;
        }

        /// <summary>
        /// Moves Current to the next node. If wrap is true, moving past the
        /// tail wraps back to the head (handy for "repeat playlist").
        /// </summary>
        public Node<T> MoveNext(bool wrap = true)
        {
            if (Current == null) return null;

            if (Current.Next != null)
                Current = Current.Next;
            else if (wrap)
                Current = Head;

            return Current;
        }

        /// <summary>Moves Current to the previous node (optionally wrapping).</summary>
        public Node<T> MovePrevious(bool wrap = true)
        {
            if (Current == null) return null;

            if (Current.Previous != null)
                Current = Current.Previous;
            else if (wrap)
                Current = Tail;

            return Current;
        }

        public void SetCurrent(Node<T> node) => Current = node;

        /// <summary>Removes every node from the list.</summary>
        public void Clear()
        {
            Head = Tail = Current = null;
            Count = 0;
        }

        /// <summary>
        /// Fisher-Yates shuffle of the node *values*, so existing Node
        /// references (and therefore the Current pointer) stay valid -
        /// only what they point at changes.
        /// </summary>
        public void Shuffle()
        {
            if (Count < 2) return;

            var values = new List<T>(Count);
            for (var n = Head; n != null; n = n.Next)
                values.Add(n.Value);

            var rng = new Random();
            for (int i = values.Count - 1; i > 0; i--)
            {
                int j = rng.Next(i + 1);
                (values[i], values[j]) = (values[j], values[i]);
            }

            int idx = 0;
            for (var n = Head; n != null; n = n.Next)
                n.Value = values[idx++];
        }

        public IEnumerator<T> GetEnumerator()
        {
            for (var n = Head; n != null; n = n.Next)
                yield return n.Value;
        }

        IEnumerator IEnumerable.GetEnumerator() => GetEnumerator();
    }
}
