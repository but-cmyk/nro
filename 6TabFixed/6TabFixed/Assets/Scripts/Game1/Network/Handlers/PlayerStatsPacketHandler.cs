namespace Game1
{
    using System;

    public class PlayerStatsPacketHandler : IPacketHandler
    {
        public bool Handle(Controller controller, Message msg)
        {
            try
            {
                switch (msg.command)
                {
                    case 6: // Currency (xu, luong, luongKhoa)
                        GameCanvas.debug("SA70", 2);
                        if (Char.myCharz() != null)
                        {
                            Char.myCharz().xu = msg.reader().readLong();
                            Char.myCharz().luong = msg.reader().readInt();
                            Char.myCharz().luongKhoa = msg.reader().readInt();
                            Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                            Char.myCharz().luongStr = mSystem.numberTostring(Char.myCharz().luong);
                            Char.myCharz().luongKhoaStr = mSystem.numberTostring(Char.myCharz().luongKhoa);
                        }
                        GameCanvas.endDlg();
                        return true;

                    case -69: // cMaxStamina
                        if (Char.myCharz() != null)
                        {
                            Char.myCharz().cMaxStamina = msg.reader().readShort();
                        }
                        return true;

                    case -68: // cStamina
                        if (Char.myCharz() != null)
                        {
                            Char.myCharz().cStamina = msg.reader().readShort();
                        }
                        return true;

                    case -97: // cNangdong
                        if (Char.myCharz() != null)
                        {
                            Char.myCharz().cNangdong = msg.reader().readInt();
                        }
                        return true;
                }
            }
            catch (Exception ex)
            {
                Cout.println("Loi PlayerStatsPacketHandler: " + ex.ToString());
            }
            return false;
        }
    }
}
