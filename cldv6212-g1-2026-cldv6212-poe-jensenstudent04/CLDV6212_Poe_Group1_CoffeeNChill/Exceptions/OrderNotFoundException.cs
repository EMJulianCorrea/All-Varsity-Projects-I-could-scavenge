using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CoffeeNChill.Functions.Exceptions;

public class OrderNotFoundException : Exception
{
    public OrderNotFoundException(string orderDate, string orderId)
        : base($"Order '{orderId}' was not found for date '{orderDate}'.")
    {
    }
}