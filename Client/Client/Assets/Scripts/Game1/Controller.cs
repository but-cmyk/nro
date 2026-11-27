namespace Game1
{
    using Game1.Assets.src.e;
    using Assets.src.f;
    using Game1.Assets.src.g;
    using System;
    using UnityEngine;
    
    public partial class Controller : IMessageHandler
    {
        protected static Controller me;
    
        protected static Controller me2;
    
        public Message messWait;
    
        public static bool isLoadingData = false;
    
        public static bool isConnectOK;
    
        public static bool isConnectionFail;
    
        public static bool isDisconnected;
    
        public static bool isMain;
    
        private float demCount;
    
        private int move;
    
        private int total;
    
        public static bool isStopReadMessage;
    
        public static MyHashTable frameHT_NEWBOSS = new MyHashTable();
    
        public const sbyte PHUBAN_TYPE_CHIENTRUONGNAMEK = 0;
    
        public const sbyte PHUBAN_START = 0;
    
        public const sbyte PHUBAN_UPDATE_POINT = 1;
    
        public const sbyte PHUBAN_END = 2;
    
        public const sbyte PHUBAN_LIFE = 4;
    
        public const sbyte PHUBAN_INFO = 5;
    
        public static Controller gI()
        {
            if (me == null)
            {
                me = new Controller();
            }
            return me;
        }
    
        public static Controller gI2()
        {
            if (me2 == null)
            {
                me2 = new Controller();
            }
            return me2;
        }
    
        public void onConnectOK(bool isMain1)
        {
            isMain = isMain1;
            mSystem.onConnectOK();
        }
    
        public void onConnectionFail(bool isMain1)
        {
            isMain = isMain1;
            mSystem.onConnectionFail();
        }
    
        public void onDisconnected(bool isMain1)
        {
            isMain = isMain1;
            mSystem.onDisconnected();
        }
    
        public void requestItemPlayer(Message msg)
        {
            try
            {
                int num = msg.reader().readUnsignedByte();
                Item item = GameScr.currentCharViewInfo.arrItemBody[num];
                item.saleCoinLock = msg.reader().readInt();
                item.sys = msg.reader().readByte();
                item.options = new MyVector();
                try
                {
                    while (msg.reader().available() > 0)
                    {
                        item.options.addElement(new ItemOption(msg.reader().readUnsignedByte(), msg.reader().readUnsignedShort()));
                    }
                }
                catch (Exception ex)
                {
                    Cout.println("Loi tairequestItemPlayer 1" + ex.ToString());
                }
                if (item.options != null && item.options.size() > 0)
                {
                    item.itemOption = new ItemOption[item.options.size()];
                    for (int i = 0; i < item.options.size(); i++)
                    {
                        item.itemOption[i] = (ItemOption)item.options.elementAt(i);
                    }
                }
            }
            catch (Exception ex2)
            {
                Cout.println("Loi tairequestItemPlayer 2" + ex2.ToString());
            }
        }
    
        public void onMessage(Message msg)
        {
            try
            {
                Char @char = null;
                Mob mob = null;
                MyVector myVector = new MyVector();
                int num = 0;
                GameCanvas.timeLoading = 15;
                if (Controller2.readMessage(msg))
                {
                    return;
                }
                if (PacketDispatcher.gI().Dispatch(this, msg))
                {
                    return;
                }
                if (handlePacketGroupA(msg, ref @char, ref mob, myVector, ref num)
                    || handlePacketGroupB(msg, ref @char, ref mob, myVector, ref num)
                    || handlePacketGroupC(msg, ref @char, ref mob, myVector, ref num)
                    || handlePacketGroupD(msg, ref @char, ref mob, myVector, ref num))
                {
                    GameCanvas.debug("SA92", 2);
                    return;
                }
                GameCanvas.debug("SA92", 2);
            }
            catch (Exception ex41)
            {
                Res.err("[Controller] [error] " + ex41.StackTrace + " msg: " + ex41.Message + " cause " + ex41.Data);
            }
            finally
            {
                if (msg != null)
                {
                    msg.cleanup();
                }
            }
        }
    
    }
}
