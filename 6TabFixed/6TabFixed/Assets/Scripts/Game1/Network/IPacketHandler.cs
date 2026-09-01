namespace Game1
{
    public interface IPacketHandler
    {
        bool Handle(Controller controller, Message msg);
    }
}
