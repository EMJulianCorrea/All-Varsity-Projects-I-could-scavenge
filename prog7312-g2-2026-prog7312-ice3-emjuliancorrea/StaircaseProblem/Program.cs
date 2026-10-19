using System;
using System.Text;
using System.Collections.Generic;
using System.Numerics;

namespace StaircaseProblem
{
    public class Program
    {
        static Dictionary<int, BigInteger> memo = new Dictionary<int, BigInteger>();

        static void Main(string[] args)
        {
            Console.WriteLine("How many stairs does the staircase have?");
            int numberInput = Convert.ToInt32(Console.ReadLine());
            while (numberInput != 0)
            {
                Console.WriteLine("There are " + numberInput + " unique ways of getting up a staircase with " + StairAlgorithm(numberInput) + " stairs.");
                Console.WriteLine("How many stairs does the staircase have now?");
                numberInput = Convert.ToInt32(Console.ReadLine());
            }
            Console.WriteLine("Closing program...");
            return;
        }

        public static BigInteger StairAlgorithm(int number)
        {
            if (number <= 1)
            {
                return 1;
            }
            int[] dp = new int[number + 1];
            dp[1] = 1;
            dp[2] = 2;

            for (int i = 3; i <= number; i++)
            {
                dp[i] = dp[i - 1] + dp[i - 2];
            }

            return dp[number];
        }
    }
}