namespace CEOTreeHierarchy
{
    public class TreeTraversal
    {
        public static void IOT<T>(TreeNode<T> node, Action<T> visit)
        {
            if (node == null) return;
            if (node.Children.Count > 0)
            {
                IOT(node.Children[0], visit);
            }
            visit(node.Value);
            for (int i = 1; i < node.Children.Count; i++)
            {
                IOT(node.Children[i], visit);
            }
        }
        // Depth-First Search (recursive)
        public static void PreOT<T>(TreeNode<T> node, Action<T> visit)
        {
            if (node == null) return;
            visit(node.Value); // Process current node
            foreach (var child in node.Children)
            {
                PreOT(child, visit);
            }
        }
        public static void PosOT<T>(TreeNode<T> node, Action<T> visit)
        {
            if (node == null) return;
            foreach (var child in node.Children)
            {
                PosOT(child, visit);
            }
            visit(node.Value);
        }

        public static void LOT<T>(TreeNode<T> root, Action<T> visit)
        {
            if (root == null) return;

            Queue<TreeNode<T>> queue = new Queue<TreeNode<T>>();
            queue.Enqueue(root);

            while (queue.Count > 0)
            {
                var current = queue.Dequeue();
                visit(current.Value);

                foreach (var child in current.Children)
                {
                    queue.Enqueue(child);
                }
            }
        }
        //Node.FindDepthFirst
    }
}