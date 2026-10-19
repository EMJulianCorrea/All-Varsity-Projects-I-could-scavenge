using System;

namespace CEOTreeHierarchy
{
    public static class Program
    {
        public static void Main(string[] args)
        {
            string[] cSuite = new string[] {"CEO: Zanele", "CIO: Thabo", "Software Development Manager: Linda", "Developer: Lindiwe", "CFO: Pieter",
        "Accountant: Muhammad", "COO: Lerato", "Operations Manager: Nomsa", "CTO: Sifiso", "Tech Lead: Megan"};

            var root = new TreeNode<string>(cSuite[0]);
            var child1 = new TreeNode<string>(cSuite[1]);
            var child2 = new TreeNode<string>(cSuite[4]);
            var child3 = new TreeNode<string>(cSuite[6]);
            var child4 = new TreeNode<string>(cSuite[8]);
            var child1_1 = new TreeNode<string>(cSuite[2]);

            root.AddChild(child1);
            root.AddChild(child2);
            root.AddChild(child3);
            root.AddChild(child4);

            child1.AddChild(child1_1);
            child2.AddChild(new TreeNode<string>(cSuite[5]));
            child3.AddChild(new TreeNode<string>(cSuite[7]));
            child4.AddChild(new TreeNode<string>(cSuite[9]));

            child1_1.AddChild(new TreeNode<string>(cSuite[3]));

            Console.WriteLine("\nInorder Traversal:\n");
            TreeTraversal.IOT(root, value => Console.WriteLine(value));
            Console.WriteLine("\nPre-order Traversal:\n");
            TreeTraversal.PreOT(root, value => Console.WriteLine(value));
            Console.WriteLine("\nPost-order Traversal:\n");
            TreeTraversal.PosOT(root, value => Console.WriteLine(value));
            Console.WriteLine("\nLevel Order Traversal:\n");
            TreeTraversal.LOT(root, value => Console.WriteLine(value));

            Console.WriteLine("\nPlease enter the name of an employee or their title:");
            string searchTerm = Console.ReadLine();
            while (searchTerm != "exit")
            {

            var results = TreeSearchHelper.SearchByTerm(root, searchTerm);

            if (results.Count == 0)
            {
                Console.WriteLine($"\nNo matches found for \"{searchTerm}\".");
            }
            else
            {
                Console.WriteLine($"\nFound {results.Count} match(es):");
                foreach (var node in results)
                {
                    Console.WriteLine($" - {node.Value}");
                }
            }
            Console.WriteLine("\nPlease enter the name of an employee or their title (search 'exit' to end program):");
            searchTerm = Console.ReadLine();
            }
            Console.WriteLine("\nShutting down...");
        }
    }
}