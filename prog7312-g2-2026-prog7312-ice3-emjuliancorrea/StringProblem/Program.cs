using System;
using System.Text;
using System.Collections.Generic;
using System.Numerics;

namespace StringProblem
{
    public class Program
    {

        public static void Main(string[] args)
        {
            Console.WriteLine("Put in some text to be changed");
            string stringInput = Console.ReadLine();
            while (stringInput != "exit")
            {
                Console.WriteLine(FindPerms(stringInput));
                Console.WriteLine("Add some more text");
                stringInput = Console.ReadLine();
            }
            Console.WriteLine("Closing program...");
            return;
        }
        public static List<string> FindPerms(string str)
        {
            if (str.Length == 1)
            {
                return new List<string> { str };
            }

            List<string> all = new List<string>();

            for (int i = 0; i < str.Length; i++)
            {
                char currentLetter = str[i];
                string remainingLetters = str.Substring(0, i) + str.Substring(i + 1);
                List<string> permsOfRemainingLetters = FindPerms(remainingLetters);

                foreach (string subPerm in permsOfRemainingLetters)
                {
                    all.Add(currentLetter + subPerm);
                }
            }

            return all;
        }
    }
}