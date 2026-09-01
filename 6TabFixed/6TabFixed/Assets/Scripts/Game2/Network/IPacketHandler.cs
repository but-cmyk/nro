namespace Game2
{
    public interface IPacketHandler
    {
        bool Handle(Controller controller, Message msg);
    }
}
