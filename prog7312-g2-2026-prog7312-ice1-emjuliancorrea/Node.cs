using GadgetList;

namespace GadgetList
{
    public class Node
    {
        public Gadget Data;
        public Node Next;

        public Node(Gadget gadget)
        {
            Data = gadget;
            Next = null;
        }
    }
}
