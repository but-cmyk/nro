namespace Game1
{
    using System;
    using System.Collections.Generic;

    public class PacketDispatcher
    {
        private static PacketDispatcher _instance;
        public static PacketDispatcher gI()
        {
            if (_instance == null)
            {
                _instance = new PacketDispatcher();
            }
            return _instance;
        }

        private readonly Dictionary<sbyte, IPacketHandler> _handlers = new Dictionary<sbyte, IPacketHandler>();

        public PacketDispatcher()
        {
            RegisterHandlers();
        }

        public void Register(sbyte command, IPacketHandler handler)
        {
            _handlers[command] = handler;
        }

        public bool Dispatch(Controller controller, Message msg)
        {
            if (msg == null) return false;
            if (_handlers.TryGetValue(msg.command, out IPacketHandler handler))
            {
                return handler.Handle(controller, msg);
            }
            return false;
        }

        private void RegisterHandlers()
        {
            AuthPacketHandler auth = new AuthPacketHandler();
            _handlers[-26] = auth;
        }
    }
}
