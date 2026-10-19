using System;
using System.Text;
using System.Collections.Generic;
using System.Numerics;

namespace FibonacciSequence
{
    public class Program
    {
        static Dictionary<int, BigInteger> memo = new Dictionary<int, BigInteger>();

        static void Main(string[] args)
        {
            Console.WriteLine("Please enter a number");
            int numberInput = Convert.ToInt32(Console.ReadLine());
            while (numberInput != 0)
            {
                Console.WriteLine("The number in position " + numberInput + " of the FibonacciSequence is " + FibonacciLaw(numberInput));
                Console.WriteLine("Please enter a different number");
                numberInput = Convert.ToInt32(Console.ReadLine());
            }
            Console.WriteLine("Closing program...");
            return;
        }

        public static BigInteger FibonacciLaw(int number)
        {
            if (number <= 1)
            {
                return 1;
            }

            if (memo.ContainsKey(number))
            {
                return memo[number];
            }

            BigInteger result = FibonacciLaw(number - 2) + FibonacciLaw(number - 1);

            memo[number] = result;

            return result;
        }
    }
}