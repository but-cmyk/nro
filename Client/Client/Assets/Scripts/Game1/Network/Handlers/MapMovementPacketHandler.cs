namespace Game1
{
    using System;

    /// <summary>
    /// MapMovementPacketHandler: Chuyên trách phân tích và xử lý các gói tin chuyển map, định vị và di chuyển nhanh.
    /// Opcodes: -22 (chuẩn bị chuyển map), 46 (set reset point / respawn point), 58 (move fast / tốc biến), -91 (danh sách map teleport).
    /// </summary>
    public class MapMovementPacketHandler : IPacketHandler
    {
        public bool Handle(Controller controller, Message msg)
        {
            try
            {
                switch (msg.command)
                {
                    case NetworkOpcodes.CHANGE_MAP_PREPARE:
                        {
                            GameCanvas.debug("SA65", 2);
                            Char.isLockKey = true;
                            Char.ischangingMap = true;
                            GameScr.gI().timeStartMap = 0;
                            GameScr.gI().timeLengthMap = 0;
                            Char.myCharz().mobFocus = null;
                            Char.myCharz().npcFocus = null;
                            Char.myCharz().charFocus = null;
                            Char.myCharz().itemFocus = null;
                            Char.myCharz().focus.removeAllElements();
                            Char.myCharz().testCharId = -9999;
                            Char.myCharz().killCharId = -9999;
                            GameCanvas.resetBg();
                            GameScr.gI().resetButton();
                            GameScr.gI().center = null;
                            if (Effect.vEffData.size() > 15)
                            {
                                for (int i = 0; i < 5; i++)
                                {
                                    Effect.vEffData.removeElementAt(0);
                                }
                            }
                            return true;
                        }

                    case NetworkOpcodes.RESET_POINT:
                        {
                            GameCanvas.debug("SA5", 2);
                            Cout.LogWarning("Controler RESET_POINT  " + Char.ischangingMap);
                            Char.isLockKey = false;
                            Char.myCharz().setResetPoint(msg.reader().readShort(), msg.reader().readShort());
                            return true;
                        }

                    case NetworkOpcodes.MOVE_FAST:
                        {
                            GameCanvas.debug("SZ7", 2);
                            int charId = msg.reader().readInt();
                            Char target = ((charId != Char.myCharz().charID) ? GameScr.findCharInMap(charId) : Char.myCharz());
                            if (target != null)
                            {
                                target.moveFast = new short[3];
                                target.moveFast[0] = 0;
                                short x = msg.reader().readShort();
                                short y = msg.reader().readShort();
                                target.moveFast[1] = x;
                                target.moveFast[2] = y;
                                try
                                {
                                    int nextId = msg.reader().readInt();
                                    Char target2 = ((nextId != Char.myCharz().charID) ? GameScr.findCharInMap(nextId) : Char.myCharz());
                                    if (target2 != null)
                                    {
                                        target2.cx = x;
                                        target2.cy = y;
                                    }
                                }
                                catch (Exception exMoveFast)
                                {
                                    Cout.println("Loi MOVE_FAST " + exMoveFast.ToString());
                                }
                            }
                            return true;
                        }

                    case NetworkOpcodes.MAP_TRANS:
                        {
                            sbyte count = msg.reader().readByte();
                            GameCanvas.panel.mapNames = new string[count];
                            GameCanvas.panel.planetNames = new string[count];
                            for (int j = 0; j < count; j++)
                            {
                                GameCanvas.panel.mapNames[j] = msg.reader().readUTF();
                                GameCanvas.panel.planetNames[j] = msg.reader().readUTF();
                            }
                            GameCanvas.panel.setTypeMapTrans();
                            GameCanvas.panel.show();
                            return true;
                        }
                }
            }
            catch (Exception ex)
            {
                Res.err("[MapMovementPacketHandler] Error handling cmd " + msg.command + ": " + ex.Message);
            }
            return false;
        }
    }
}
