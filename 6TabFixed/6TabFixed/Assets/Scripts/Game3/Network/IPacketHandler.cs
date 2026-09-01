namespace Game3
{
    public interface IPacketHandler
    {
        bool Handle(Controller controller, Message msg);
    }
}
