namespace CEOTreeHierarchy
{
    public static class TreeSearchHelper
    {
        public static List<TreeNode<string>> SearchByTerm(TreeNode<string> root, string searchTerm)
        {
            if (string.IsNullOrWhiteSpace(searchTerm))
                return new List<TreeNode<string>>();

            return root.FindAllNodes(value =>
                value.Contains(searchTerm, StringComparison.OrdinalIgnoreCase));
        }
    }
}