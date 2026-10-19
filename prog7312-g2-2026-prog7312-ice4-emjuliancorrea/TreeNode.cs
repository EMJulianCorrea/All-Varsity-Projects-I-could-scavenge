using System;
using System.Collections.Generic;

namespace CEOTreeHierarchy
{
    public class TreeNode<T>
    {
        public T Value { get; set; }
        public List<TreeNode<T>> Children { get; set; } = new List<TreeNode<T>>();

        public TreeNode(T value)
        {
            Value = value;
        }

        public void AddChild(TreeNode<T> child)
        {
            if (child == null)
                throw new ArgumentNullException(nameof(child), "Child node cannot be null.");
            Children.Add(child);
        }

        // BFS search - returns first match
        public TreeNode<T> FindNode(Func<T, bool> predicate)
        {
            if (predicate == null)
                throw new ArgumentNullException(nameof(predicate));

            var queue = new Queue<TreeNode<T>>();
            queue.Enqueue(this);

            while (queue.Count > 0)
            {
                var current = queue.Dequeue();

                if (predicate(current.Value))
                    return current;

                foreach (var child in current.Children)
                    queue.Enqueue(child);
            }

            return null;
        }

        // BFS search - returns all matches
        public List<TreeNode<T>> FindAllNodes(Func<T, bool> predicate)
        {
            if (predicate == null)
                throw new ArgumentNullException(nameof(predicate));

            var results = new List<TreeNode<T>>();
            var queue = new Queue<TreeNode<T>>();
            queue.Enqueue(this);

            while (queue.Count > 0)
            {
                var current = queue.Dequeue();

                if (predicate(current.Value))
                    results.Add(current);

                foreach (var child in current.Children)
                    queue.Enqueue(child);
            }

            return results;
        }
    }
}