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
            Register(auth, NetworkOpcodes.LOGOUT);

            PlayerStatsPacketHandler stats = new PlayerStatsPacketHandler();
            Register(stats, NetworkOpcodes.UPDATE_HP, NetworkOpcodes.UPDATE_MP, NetworkOpcodes.UPDATE_EXP, NetworkOpcodes.UPDATE_NANG_DONG);

            TaskNpcPacketHandler npc = new TaskNpcPacketHandler();
            Register(npc, NetworkOpcodes.TASK_UPDATE, NetworkOpcodes.NPC_MENU, NetworkOpcodes.NPC_CHAT);

            ClanPacketHandler clan = new ClanPacketHandler();
            Register(clan, NetworkOpcodes.CLAN_MESSAGE, NetworkOpcodes.CLAN_INFO, NetworkOpcodes.CLAN_UPDATE, NetworkOpcodes.CLAN_MEMBER, NetworkOpcodes.CLAN_SEARCH, NetworkOpcodes.CLAN_CREATE_INFO);

            TradePacketHandler trade = new TradePacketHandler();
            Register(trade, NetworkOpcodes.TRADE_ACTION, NetworkOpcodes.TRADE_ORDER);

            ItemPacketHandler item = new ItemPacketHandler();
            Register(item, NetworkOpcodes.ITEM_MAP_REMOVE, NetworkOpcodes.ITEM_MAP_PICK, NetworkOpcodes.ITEM_MAP_OTHER_PICK, NetworkOpcodes.ITEM_MAP_DROP, NetworkOpcodes.ITEM_MAP_OTHER_DROP, NetworkOpcodes.ITEM_MAP_ADD);

            MapMovementPacketHandler mapMove = new MapMovementPacketHandler();
            Register(mapMove, NetworkOpcodes.CHANGE_MAP_PREPARE, NetworkOpcodes.RESET_POINT, NetworkOpcodes.MOVE_FAST, NetworkOpcodes.MAP_TRANS);

            CombatPacketHandler combat = new CombatPacketHandler();
            Register(combat, NetworkOpcodes.CHAR_INJURE, NetworkOpcodes.REVIVE, NetworkOpcodes.MOB_ME_ATTACK, NetworkOpcodes.SKILL_COOLDOWN);
        }
    }
}
