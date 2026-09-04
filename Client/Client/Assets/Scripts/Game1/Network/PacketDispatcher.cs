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

        public void Register(IPacketHandler handler, params sbyte[] commands)
        {
            if (handler == null || commands == null) return;
            for (int i = 0; i < commands.Length; i++)
            {
                _handlers[commands[i]] = handler;
            }
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
            Register(auth, -26);

            PlayerStatsPacketHandler stats = new PlayerStatsPacketHandler();
            Register(stats, 6, -69, -68, -97);

            TaskNpcPacketHandler npc = new TaskNpcPacketHandler();
            Register(npc, -70, 38, 32);

            ClanPacketHandler clan = new ClanPacketHandler();
            Register(clan, -51, -53, -52, -50, -47, -46);
        }
    }
}
