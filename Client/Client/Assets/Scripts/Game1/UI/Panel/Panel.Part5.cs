namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        private void paintFriend(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                mFont.tahoma_7_green2.drawString(g, mResources.no_friend, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
                return;
            }
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = 24;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll + num3;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = wScroll - num3;
                int h2 = ITEM_HEIGHT - 1;
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num4, num5, num6, h2);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num, num2, num3, h);
                InfoItem infoItem = (InfoItem)vFriend.elementAt(i);
                if (infoItem.charInfo.headICON != -1)
                {
                    SmallImage.drawSmallImage(g, infoItem.charInfo.headICON, num, num2, 0, 0);
                }
                else
                {
                    Part part = GameScr.parts[infoItem.charInfo.head];
                    SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + part.pi[Char.CharInfo[0][0][0]].dx, num2 + 3 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                }
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                if (infoItem.isOnline)
                {
                    mFont.tahoma_7b_green.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                    mFont.tahoma_7_blue.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
                }
                else
                {
                    mFont.tahoma_7_grey.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                    mFont.tahoma_7_grey.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        public void paintPlayerMenu(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            if (vPlayerMenu == null || vPlayerMenu.size() == 0)
            {
                mFont.tahoma_7b_dark.drawString(g, "Không có tuỳ chọn", xScroll + wScroll / 2, yScroll + hScroll / 2 - 5, mFont.CENTER);
                return;
            }
            for (int i = 0; i < vPlayerMenu.size(); i++)
            {
                int x = xScroll;
                int num = yScroll + i * ITEM_HEIGHT;
                int num2 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
                {
                    Command command = (Command)vPlayerMenu.elementAt(i);
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);
                    if (command.caption2.Equals(string.Empty))
                    {
                        mFont.tahoma_7b_dark.drawString(g, command.caption, xScroll + wScroll / 2, num + 6, mFont.CENTER);
                        continue;
                    }
                    mFont.tahoma_7b_dark.drawString(g, command.caption, xScroll + wScroll / 2, num + 1, mFont.CENTER);
                    mFont.tahoma_7b_dark.drawString(g, command.caption2, xScroll + wScroll / 2, num + 11, mFont.CENTER);
                }
            }
            paintScrollArrow(g);
        }
    
    
        private void paintArchivement(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                mFont.tahoma_7_green2.drawString(g, mResources.no_mission, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
            }
            else
            {
                if (Char.myCharz().arrArchive == null || Char.myCharz().arrArchive.Length != currentListLength)
                {
                    mFont.tahoma_7b_dark.drawString(g, "Đang tải danh sách nhiệm vụ...", xScroll + wScroll / 2, yScroll + hScroll / 2 - 5, mFont.CENTER);
                    return;
                }
                for (int i = 0; i < currentListLength; i++)
                {
                    int num = xScroll;
                    int num2 = yScroll + i * ITEM_HEIGHT;
                    int num3 = wScroll;
                    int num4 = ITEM_HEIGHT - 1;
                    Archivement archivement = Char.myCharz().arrArchive[i];
                    g.setColor((i != selected || ((archivement.isRecieve || archivement.isFinish) && (!archivement.isRecieve || !archivement.isFinish))) ? 15196114 : 16383818);
                    g.fillRect(num, num2, num3, num4);
                    if (archivement == null)
                    {
                        continue;
                    }
                    if (!archivement.isFinish)
                    {
                        mFont.tahoma_7.drawString(g, archivement.info1, num + 5, num2, 0);
                        mFont.tahoma_7_green.drawString(g, archivement.money + " " + mResources.RUBY, num + num3 - 5, num2, mFont.RIGHT);
                        mFont.tahoma_7_red.drawString(g, archivement.info2, num + 5, num2 + 11, 0);
                    }
                    else if (archivement.isFinish && !archivement.isRecieve)
                    {
                        mFont.tahoma_7.drawString(g, archivement.info1, num + 5, num2, 0);
                        mFont.tahoma_7_blue.drawString(g, mResources.reward_mission + archivement.money + " " + mResources.RUBY, num + 5, num2 + 11, 0);
                        if (i == selected)
                        {
                            mFont.tahoma_7b_green2.drawString(g, mResources.receive_upper, num + num3 - 20, num2 + 6, mFont.CENTER);
                            mFont.tahoma_7b_dark.drawString(g, mResources.receive_upper, num + num3 - 20, num2 + 6, mFont.CENTER);
                        }
                        else
                        {
                            g.drawImage(GameScr.imgLbtn2, num + num3 - 20, num2 + num4 / 2, StaticObj.VCENTER_HCENTER);
                            mFont.tahoma_7b_dark.drawString(g, mResources.receive_upper, num + num3 - 20, num2 + 6, mFont.CENTER);
                        }
                    }
                    else if (archivement.isFinish && archivement.isRecieve)
                    {
                        mFont.tahoma_7_green.drawString(g, archivement.info1, num + 5, num2, 0);
                        mFont.tahoma_7_green.drawString(g, archivement.info2, num + 5, num2 + 11, 0);
                    }
                }
                paintScrollArrow(g);
            }
        }
    
        private void paintItemStar(mGraphics g, string opt, int x, int y)
        {
        }
    
    
    
        private void paintTab(mGraphics g)
        {
           
            if (type == TYPE_GAMEINFO || type == TYPE_GAMEINFOSUB)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.gameInfo, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_ACCOUNT)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.account, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_AUTO)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.autoFunction, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }

            if (type == TYPE_OPTION)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.option, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_FLAG)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.change_flag, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_GIAODICH && Equals(GameCanvas.panel2))
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.item_receive2, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_COMBINE && GameCanvas.panel2 != null)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.UPGRADE, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_FRIEND)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.friend, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_ENEMY)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.enemy, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_TOP)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, topName, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_BOX && GameCanvas.panel2 != null)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.chest, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_ARCHIVEMENT)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.achievement_mission, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_ZONE)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.select_zone, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_MAPTRANS)
            {
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                mFont.tahoma_7b_dark.drawString(g, mResources.select_map, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                return;
            }
            if (type == TYPE_MAP)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.map, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                return;
            }
            if (type == TYPE_BODY)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.trangbi, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                return;
            }
            if (type == TYPE_KIGUI)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.kigui, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                return;
            }
            if (type == TYPE_MESS)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.msg, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                return;
            }
            if (type == 10)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.wat_do_u_want, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                g.setColor(13524492);
                g.fillRect(X + 1, 78, W - 2, 1);
                return;
            }
            if (currentTabIndex == 3 && mainTabName.Length != 4)
            {
                g.translate(-cmx, 0);
            }
            for (int i = 0; i < currentTabName.Length; i++)
            {
                g.setColor((i != currentTabIndex) ? 16773296 : 6805896);
                PopUp.paintPopUp(g, startTabPos + i * TAB_W, 52, TAB_W - 1, 25, (i == currentTabIndex) ? 1 : 0, true);
                if (i == keyTouchTab)
                {
                    g.drawImage(ItemMap.imageFlare, startTabPos + i * TAB_W + TAB_W / 2, 62, 3);
                }
                mFont mFont2 = ((i != currentTabIndex) ? mFont.tahoma_7_grey : mFont.tahoma_7_green2);
                if (currentTabName[i].Length > 1 && !currentTabName[i][1].Equals(string.Empty))
                {
                    mFont2.drawString(g, currentTabName[i][0], startTabPos + i * TAB_W + TAB_W / 2, 53, mFont.CENTER);
                    mFont2.drawString(g, currentTabName[i][1], startTabPos + i * TAB_W + TAB_W / 2, 64, mFont.CENTER);
                }
                else
                {
                    mFont2.drawString(g, currentTabName[i][0], startTabPos + i * TAB_W + TAB_W / 2, 59, mFont.CENTER);
                }
                if (type == 0 && currentTabName.Length == 5 && GameScr.isNewClanMessage && GameCanvas.gameTick % 4 == 0)
                {
                    g.drawImage(ItemMap.imageFlare, startTabPos + 3 * TAB_W + TAB_W / 2, 77, mGraphics.BOTTOM | mGraphics.HCENTER);
                }
            }
            g.setColor(13524492);
            g.fillRect(1, 78, W - 2, 1);
        }
    
        private void paintBottomMoneyInfo(mGraphics g)
        {
            if (type != 13 || (currentTabIndex != 2 && !Equals(GameCanvas.panel2)))
            {
                g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
                g.setColor(11837316);
                g.fillRect(X + 1, H - 15, W - 2, 14);
                g.setColor(13524492);
                g.fillRect(X + 1, H - 15, W - 2, 1);
                g.drawImage(imgXu, X + 11, H - 7, 3);
                g.drawImage(imgLuong, X + 75, H - 8, 3);
                mFont.tahoma_7_yellow.drawString(g, Char.myCharz().xuStr, X + 24, H - 13, mFont.LEFT, mFont.tahoma_7_grey);
                mFont.tahoma_7_yellow.drawString(g, Char.myCharz().luongStr, X + 85, H - 13, mFont.LEFT, mFont.tahoma_7_grey);
                g.drawImage(imgLuongKhoa, X + 130, H - 8, 3);
                mFont.tahoma_7_yellow.drawString(g, Char.myCharz().luongKhoaStr, X + 140, H - 13, mFont.LEFT, mFont.tahoma_7_grey);
            }
        }
    
        private void paintClanInfo(mGraphics g)
        {
            if (Char.myCharz().clan == null)
            {
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), 25, 50, 0, 33);
                mFont.tahoma_7b_white.drawString(g, mResources.not_join_clan, (wScroll - 50) / 2 + 50, 20, mFont.CENTER);
            }
            else if (!isViewMember)
            {
                Clan clan = Char.myCharz().clan;
                if (clan != null)
                {
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), 25, 50, 0, 33);
                    mFont.tahoma_7b_white.drawString(g, clan.name, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
                    mFont.tahoma_7_yellow.drawString(g, mResources.achievement_point + ": " + clan.powerPoint, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
                    mFont.tahoma_7_yellow.drawString(g, mResources.clan_point + ": " + clan.clanPoint, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
                    mFont.tahoma_7_yellow.drawString(g, mResources.level + ": " + clan.level, 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
                    TextInfo.paint(g, clan.slogan, 60, 38, wScroll - 70, ITEM_HEIGHT, mFont.tahoma_7_yellow);
                }
            }
            else
            {
                Clan clan2 = ((currClan == null) ? Char.myCharz().clan : currClan);
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), 25, 50, 0, 33);
                mFont.tahoma_7b_white.drawString(g, clan2.name, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
                mFont.tahoma_7_yellow.drawString(g, mResources.member + ": " + clan2.currMember + "/" + clan2.maxMember, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
                mFont.tahoma_7_yellow.drawString(g, mResources.clan_leader + ": " + clan2.leaderName, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
                TextInfo.paint(g, clan2.slogan, 60, 38, wScroll - 70, ITEM_HEIGHT, mFont.tahoma_7_yellow);
            }
        }
    
        private void paintToolInfo(mGraphics g)
        {
            mFont.tahoma_7b_white.drawString(g, mResources.dragon_ball + " " + GameMidlet.VERSION, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
            mFont.tahoma_7_yellow.drawString(g, mResources.character + ": " + Char.myCharz().cName, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            string text = ((!GameCanvas.loginScr.tfUser.getText().Equals(string.Empty)) ? GameCanvas.loginScr.tfUser.getText() : mResources.not_register_yet);
            mFont.tahoma_7_yellow.drawString(g, mResources.account_server + " " + ServerListScreen.nameServer[ServerListScreen.ipSelect] + ": " + text, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintGiaoDichInfo(mGraphics g)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.select_item, 60, 4, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.lock_trade, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.wait_opp_lock_trade, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.press_done, 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintMyInfo(mGraphics g)
        {
            paintCharInfo(g, Char.myCharz());
        }
    
        private void paintPetInfo(mGraphics g)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.power + ": " + NinjaUtil.getMoneys(Char.myPetz().cPower), X + 60, 4, mFont.LEFT, mFont.tahoma_7_grey);
            if (Char.myPetz().cPower > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, (!Char.myPetz().me) ? Char.myPetz().currStrLevel : Char.myPetz().getStrLevel(), X + 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            }
            if (Char.myPetz().cDamFull > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.hit_point + " :" + NinjaUtil.getMoneys(Char.myPetz().cDamFull), X + 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
            }
            if (Char.myPetz().cMaxStamina > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.vitality, X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
                g.drawImage(GameScr.imgMPLost, X + 100, 41, 0);
                int num = Char.myPetz().cStamina * mGraphics.getImageWidth(GameScr.imgMP) / Char.myPetz().cMaxStamina;
                g.setClip(100, X + 41, num, 20);
                g.drawImage(GameScr.imgMP, X + 100, 41, 0);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        }
    
        private void paintCharInfo(mGraphics g, Char c)
        {
            mFont.tahoma_7b_white.drawString(g, ((GameScr.isNewMember == 1) ? "       " : string.Empty) + c.cName, X + 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
            if (GameScr.isNewMember == 1)
            {
                SmallImage.drawSmallImage(g, 5427, X + 55, 4, 0, 0);
            }
            if (c.cMaxStamina > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.vitality, X + 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
                g.drawImage(GameScr.imgMPLost, X + 95, 19, 0);
                int num = c.cStamina * mGraphics.getImageWidth(GameScr.imgMP) / c.cMaxStamina;
                g.setClip(95, X + 19, num, 20);
                g.drawImage(GameScr.imgMP, X + 95, 19, 0);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            if (c.cPower > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, (!c.me) ? c.currStrLevel : c.getStrLevel(), X + 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
            }
            mFont.tahoma_7_yellow.drawString(g, mResources.power + ": " + NinjaUtil.getMoneys(c.cPower), X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintCharInfo(mGraphics g, Char c, int x, int y)
        {
            mFont.tahoma_7b_white.drawString(g, ((GameScr.isNewMember == 1) ? "       " : string.Empty) + c.cName, x + 60, y + 4, mFont.LEFT, mFont.tahoma_7b_dark);
            if (GameScr.isNewMember == 1)
            {
                SmallImage.drawSmallImage(g, 5427, x + 55, y + 4, 0, 0);
            }
            if (c.cMaxStamina > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.vitality, x + 60, y + 16, mFont.LEFT, mFont.tahoma_7_grey);
                g.drawImage(GameScr.imgMPLost, x + 95, y + 19, 0);
                int num = c.cStamina * mGraphics.getImageWidth(GameScr.imgMP) / c.cMaxStamina;
                g.drawImage(GameScr.imgMP, x + 95, y + 19, 0);
            }
            if (c.cPower > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, (!c.me) ? c.currStrLevel : c.getStrLevel(), x + 60, y + 27, mFont.LEFT, mFont.tahoma_7_grey);
            }
            mFont.tahoma_7_yellow.drawString(g, mResources.power + ": " + NinjaUtil.getMoneys(c.cPower), x + 60, y + 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintZoneInfo(mGraphics g)
        {
            mFont.tahoma_7b_white.drawString(g, mResources.zone + " " + TileMap.zoneID, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
            mFont.tahoma_7_yellow.drawString(g, TileMap.mapName, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7b_white.drawString(g, TileMap.zoneID + string.Empty, 25, 27, mFont.CENTER);
        }
    
        public int getCompare(Item item)
        {
            if (item == null || item.template == null)
            {
                return -1;
            }
            if (item.isTypeBody())
            {
                if (item.itemOption == null || item.itemOption.Length == 0 || item.itemOption[0] == null || item.itemOption[0].optionTemplate == null)
                {
                    return -1;
                }
                ItemOption itemOption = item.itemOption[0];
                if (itemOption.optionTemplate.id == 22 && GameScr.gI().iOptionTemplates != null && GameScr.gI().iOptionTemplates.Length > 6)
                {
                    itemOption.optionTemplate = GameScr.gI().iOptionTemplates[6];
                    itemOption.param *= 1000;
                }
                if (itemOption.optionTemplate.id == 23 && GameScr.gI().iOptionTemplates != null && GameScr.gI().iOptionTemplates.Length > 7)
                {
                    itemOption.optionTemplate = GameScr.gI().iOptionTemplates[7];
                    itemOption.param *= 1000;
                }
                Item item2 = null;
                if (Char.myCharz() != null && Char.myCharz().arrItemBody != null)
                {
                    for (int i = 0; i < Char.myCharz().arrItemBody.Length; i++)
                    {
                        Item item3 = Char.myCharz().arrItemBody[i];
                        if (item3 != null && item3.template != null && item3.itemOption != null && item3.template.type == item.template.type)
                        {
                            item2 = item3;
                            break;
                        }
                    }
                }
                if (item2 == null)
                {
                    isUp = true;
                    return itemOption.param;
                }
                int num = 0;
                num = ((item2.itemOption == null || item2.itemOption.Length == 0 || item2.itemOption[0] == null) ? itemOption.param : (itemOption.param - item2.itemOption[0].param));
                if (num < 0)
                {
                    isUp = false;
                }
                else
                {
                    isUp = true;
                }
                return num;
            }
            return 0;
        }
    
        private void paintMapInfo(mGraphics g)
        {
            mFont.tahoma_7b_white.drawString(g, mResources.MENUGENDER[TileMap.planetID], 60, 4, mFont.LEFT);
            string text = string.Empty;
            if (TileMap.mapID >= 135 && TileMap.mapID <= 138)
            {
                text = " " + mResources.tang + TileMap.zoneID;
            }
            mFont.tahoma_7_yellow.drawString(g, TileMap.mapName + text, 60, 16, mFont.LEFT);
            mFont.tahoma_7b_white.drawString(g, mResources.quest_place + ": ", 60, 27, mFont.LEFT);
            if (GameScr.getTaskMapId() >= 0 && GameScr.getTaskMapId() <= TileMap.mapNames.Length - 1)
            {
                mFont.tahoma_7_yellow.drawString(g, TileMap.mapNames[GameScr.getTaskMapId()], 60, 38, mFont.LEFT);
            }
            else
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.random, 60, 38, mFont.LEFT);
            }
        }
    
        private void paintShopInfo(mGraphics g)
        {
            if (currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null)
            {
                paintMyInfo(g);
            }
            else if (selected < 0)
            {
                if (typeShop != 2)
                {
                    mFont.tahoma_7_white.drawString(g, mResources.say_hello, X + 60, 14, 0);
                    mFont.tahoma_7_white.drawString(g, strWantToBuy, X + 60, 26, 0);
                    return;
                }
                mFont.tahoma_7_white.drawString(g, mResources.say_hello, X + 60, 5, 0);
                mFont.tahoma_7_white.drawString(g, strWantToBuy, X + 60, 17, 0);
                mFont.tahoma_7_white.drawString(g, mResources.page + " " + (currPageShop[currentTabIndex] + 1) + "/" + maxPageShop[currentTabIndex], X + 60, 29, 0);
            }
            else
            {
                int tabIdx = (type == 17 || (typeShop == 2 && currentTabIndex == 4)) ? 4 : currentTabIndex;
                if (Char.myCharz().arrItemShop == null || tabIdx < 0 || tabIdx >= Char.myCharz().arrItemShop.Length || Char.myCharz().arrItemShop[tabIdx] == null || selected < 0 || selected >= Char.myCharz().arrItemShop[tabIdx].Length)
                {
                    return;
                }
                Item item = Char.myCharz().arrItemShop[tabIdx][selected];
                if (item != null)
                {
                    if (Equals(GameCanvas.panel) && currentTabIndex <= 3 && typeShop == 2)
                    {
                        mFont.tahoma_7b_white.drawString(g, mResources.page + " " + (currPageShop[currentTabIndex] + 1) + "/" + maxPageShop[currentTabIndex], X + 55, 4, 0);
                    }
                    mFont.tahoma_7b_white.drawString(g, item.template.name, X + 55, 24, 0);
                    string st = mResources.pow_request + " " + Res.formatNumber(item.template.strRequire);
                    long currentPower = (type == 21 && Char.myPetz() != null) ? Char.myPetz().cPower : Char.myCharz().cPower;
                    if (item.template.strRequire > currentPower)
                    {
                        mFont.tahoma_7_yellow.drawString(g, st, X + 55, 35, 0);
                    }
                    else
                    {
                        mFont.tahoma_7_green.drawString(g, st, X + 55, 35, 0);
                    }
                }
            }
        }
    
        private void paintItemBoxInfo(mGraphics g)
        {
            string st = mResources.used + ": " + hasUse + "/" + Char.myCharz().arrItemBox.Length + " " + mResources.place;
            mFont.tahoma_7b_white.drawString(g, mResources.chest, 60, 4, 0);
            mFont.tahoma_7_yellow.drawString(g, st, 60, 16, 0);
        }
    
        private void paintSkillInfo(mGraphics g)
        {
            mFont.tahoma_7_white.drawString(g, "Top " + Char.myCharz().rank, X + 45 + (W - 50) / 2, 2, mFont.CENTER);
            mFont.tahoma_7_yellow.drawString(g, mResources.potential_point, X + 45 + (W - 50) / 2, 14, mFont.CENTER);
            mFont.tahoma_7_white.drawString(g, string.Empty + NinjaUtil.getMoneys(Char.myCharz().cTiemNang), X + ((GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0) + 45 + (W - 50) / 2, 26, mFont.CENTER);
            mFont.tahoma_7_yellow.drawString(g, mResources.active_point + ": " + NinjaUtil.getMoneys(Char.myCharz().cNangdong), X + ((GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0) + 45 + (W - 50) / 2, 38, mFont.CENTER);
        }
    
        private void paintItemBodyBagInfo(mGraphics g)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.HP + ": " + NinjaUtil.getMoneys(Char.myCharz().cHP) + " / " + NinjaUtil.getMoneys(Char.myCharz().cHPFull), X + 60, 2, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.KI + ": " + NinjaUtil.getMoneys(Char.myCharz().cMP) + " / " + NinjaUtil.getMoneys(Char.myCharz().cMPFull), X + 60, 14, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.hit_point + ": " + NinjaUtil.getMoneys(Char.myCharz().cDamFull), X + 60, 26, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.armor + ": " + Char.myCharz().cDefull + ", " + mResources.critical + ": " + Char.myCharz().cCriticalFull + "%", X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintItemBodyBagInfo(mGraphics g, int x, int y)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.HP + ": " + NinjaUtil.getMoneys(Char.myCharz().cHP) + " / " + NinjaUtil.getMoneys(Char.myCharz().cHPFull), x, y + 2, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.KI + ": " + NinjaUtil.getMoneys(Char.myCharz().cMP) + " / " + NinjaUtil.getMoneys(Char.myCharz().cMPFull), x, y + 14, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.hit_point + ": " + NinjaUtil.getMoneys(Char.myCharz().cDamFull), x, y + 26, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.armor + ": " + Char.myCharz().cDefull + ", " + mResources.critical + ": " + Char.myCharz().cCriticalFull + "%", x, y + 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintTopInfo(mGraphics g)
        {
            g.setClip(X + 1, Y, W - 2, yScroll - 2);
            g.setColor(9993045);
            g.fillRect(X, Y, W - 2, 50);
            switch (type)
            {
                case 13:
                    if (currentTabIndex == 0 || currentTabIndex == 1)
                    {
                        if (Equals(GameCanvas.panel))
                        {
                            SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                            paintGiaoDichInfo(g);
                        }
                        if (Equals(GameCanvas.panel2) && charMenu != null)
                        {
                            SmallImage.drawSmallImage(g, charMenu.avatarz(), X + 25, 50, 0, 33);
                            paintCharInfo(g, charMenu);
                        }
                    }
                    if (currentTabIndex == 2 && charMenu != null)
                    {
                        SmallImage.drawSmallImage(g, charMenu.avatarz(), X + 25, 50, 0, 33);
                        paintCharInfo(g, charMenu);
                    }
                    break;
                case 12:
                    if (currentTabIndex == 0)
                    {
                        int id = 1410;
                        for (int i = 0; i < GameScr.vNpc.size(); i++)
                        {
                            Npc npc = (Npc)GameScr.vNpc.elementAt(i);
                            if (npc.template.npcTemplateId == idNPC)
                            {
                                id = npc.avatar;
                            }
                        }
                        SmallImage.drawSmallImage(g, id, X + 25, 50, 0, 33);
                        paintCombineInfo(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintMyInfo(g);
                    }
                    break;
                case 11:
                case 16:
                case 23:
                case 24:
                case 30:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 15:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 9:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 21:
                    if (currentTabIndex == 0)
                    {
                        SmallImage.drawSmallImage(g, Char.myPetz().avatarz(), X + 25, 50, 0, 33);
                        paintPetInfo(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, Char.myPetz().avatarz(), X + 25, 50, 0, 33);
                        paintPetStatusInfo(g);
                    }
                    if (currentTabIndex == 2)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintItemBodyBagInfo(g);
                    }
                    break;
                case 0:
                    if (currentTabIndex == 0)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintMyInfo(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        if (isnewInventory)
                        {
                            paintCharInfo(g, Char.myCharz());
                        }
                        else
                        {
                            paintItemBodyBagInfo(g);
                        }
                    }
                    if (currentTabIndex == 2)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintSkillInfo(g);
                    }
                    if (currentTabIndex == 3)
                    {
                        if (mainTabName.Length == 5)
                        {
                            paintClanInfo(g);
                        }
                        else
                        {
                            SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                            paintToolInfo(g);
                        }
                    }
                    if (currentTabIndex == 4)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintToolInfo(g);
                    }
                    break;
                case 25:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 2:
                    if (currentTabIndex == 0)
                    {
                        SmallImage.drawSmallImage(g, 526, X + 25, 50, 0, 33);
                        paintItemBoxInfo(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintItemBodyBagInfo(g);
                    }
                    break;
                case 3:
                    SmallImage.drawSmallImage(g, 561, X + 25, 50, 0, 33);
                    paintZoneInfo(g);
                    break;
                case 1:
                    if (currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    }
                    else if (Char.myCharz().npcFocus != null)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().npcFocus.avatar, X + 25, 50, 0, 33);
                    }
                    paintShopInfo(g);
                    break;
                case 4:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMapInfo(g);
                    break;
                case 7:
                case 17:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 8:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 10:
                    if (charMenu != null)
                    {
                        SmallImage.drawSmallImage(g, charMenu.avatarz(), X + 25, 50, 0, 33);
                        paintCharInfo(g, charMenu);
                    }
                    break;
                case 14:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMapInfo(g);
                    break;
                case 18:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 19:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintToolInfo(g);
                    break;
                case 20:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintToolInfo(g);
                    break;
                case 22:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintToolInfo(g);
                    break;
                case 28:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintToolInfo(g);
                    break;
                case 5:
                case 6:
                    break;
            }
        }
    
        private void paintChatManager(mGraphics g)
        {
        }
    
        private void paintChatPlayer(mGraphics g)
        {
        }
    
        private string getStatus(int status)
        {
            switch (status)
            {
                case 0:
                    return mResources.follow;
                case 1:
                    return mResources.defend;
                case 2:
                    return mResources.attack;
                case 3:
                    return mResources.gohome;
                default:
                    return "aaa";
            }
        }
    
        private void paintPetStatusInfo(mGraphics g)
        {
            mFont.tahoma_7b_white.drawString(g, "HP: " + NinjaUtil.getMoneys(Char.myPetz().cHP) + "/" + NinjaUtil.getMoneys(Char.myPetz().cHPFull), X + 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
            mFont.tahoma_7b_white.drawString(g, "MP: " + NinjaUtil.getMoneys(Char.myPetz().cMP) + "/" + NinjaUtil.getMoneys(Char.myPetz().cMPFull), X + 60, 16, mFont.LEFT, mFont.tahoma_7b_dark);
            mFont.tahoma_7_yellow.drawString(g, mResources.critical + ": " + Char.myPetz().cCriticalFull + "   " + mResources.armor + ": " + Char.myPetz().cDefull, X + 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.status + ": " + strStatus[Char.myPetz().petStatus], X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintCombineInfo(mGraphics g)
        {
            if (combineTopInfo != null)
            {
                for (int i = 0; i < combineTopInfo.Length; i++)
                {
                    mFont.tahoma_7_white.drawString(g, combineTopInfo[i], X + 45 + (W - 50) / 2, 5 + i * 14, mFont.CENTER);
                }
            }
        }
    
        private void paintInfomation(mGraphics g)
        {
        }
    
        public void paintMap(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(-cmxMap, -cmyMap);
            g.drawImage(imgMap, xScroll, yScroll, 0);
            int head = Char.myCharz().head;
            Part part = GameScr.parts[head];
            SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, xMap, yMap + 5, 0, 3);
            int align = mFont.CENTER;
            if (xMap <= 40)
            {
                align = mFont.LEFT;
            }
            if (xMap >= 220)
            {
                align = mFont.RIGHT;
            }
            mFont.tahoma_7b_yellow.drawString(g, TileMap.mapName, xMap, yMap - 12, align, mFont.tahoma_7_grey);
            int num = -1;
            if (GameScr.getTaskMapId() != -1)
            {
                for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
                {
                    if (mapId[TileMap.planetID][i] == GameScr.getTaskMapId())
                    {
                        num = i;
                        break;
                    }
                    num = 4;
                }
                if (GameCanvas.gameTick % 4 > 0)
                {
                    g.drawImage(ItemMap.imageFlare, xScroll + mapX[TileMap.planetID][num], yScroll + mapY[TileMap.planetID][num], 3);
                }
            }
            if (!GameCanvas.isTouch)
            {
                g.drawImage(imgBantay, xMove, yMove, StaticObj.TOP_RIGHT);
                for (int j = 0; j < mapX[TileMap.planetID].Length; j++)
                {
                    int num2 = mapX[TileMap.planetID][j] + xScroll;
                    int num3 = mapY[TileMap.planetID][j] + yScroll;
                    if (Res.inRect(num2 - 15, num3 - 15, 30, 30, xMove, yMove))
                    {
                        align = mFont.CENTER;
                        if (num2 <= 20)
                        {
                            align = mFont.LEFT;
                        }
                        if (num2 >= 220)
                        {
                            align = mFont.RIGHT;
                        }
                        mFont.tahoma_7b_yellow.drawString(g, TileMap.mapNames[mapId[TileMap.planetID][j]], num2, num3 - 12, align, mFont.tahoma_7_grey);
                        break;
                    }
                }
            }
            else if (!trans)
            {
                for (int k = 0; k < mapX[TileMap.planetID].Length; k++)
                {
                    int num4 = mapX[TileMap.planetID][k] + xScroll;
                    int num5 = mapY[TileMap.planetID][k] + yScroll;
                    if (Res.inRect(num4 - 15, num5 - 15, 30, 30, pX, pY))
                    {
                        align = mFont.CENTER;
                        if (num4 <= 30)
                        {
                            align = mFont.LEFT;
                        }
                        if (num4 >= 220)
                        {
                            align = mFont.RIGHT;
                        }
                        g.drawImage(imgBantay, num4, num5, StaticObj.TOP_RIGHT);
                        mFont.tahoma_7b_yellow.drawString(g, TileMap.mapNames[mapId[TileMap.planetID][k]], num4, num5 - 12, align, mFont.tahoma_7_grey);
                        break;
                    }
                }
            }
            g.translate(-g.getTranslateX(), -g.getTranslateY());
            if (num != -1)
            {
                if (mapX[TileMap.planetID][num] + xScroll < cmxMap)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 5, xScroll + 5, yScroll + hScroll / 2 - 4, 0);
                }
                if (cmxMap + wScroll < mapX[TileMap.planetID][num] + xScroll)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 6, xScroll + wScroll - 5, yScroll + hScroll / 2 - 4, StaticObj.TOP_RIGHT);
                }
                if (mapY[TileMap.planetID][num] < cmyMap)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 1, xScroll + wScroll / 2, yScroll + 5, StaticObj.TOP_CENTER);
                }
                if (mapY[TileMap.planetID][num] > cmyMap + hScroll)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 0, xScroll + wScroll / 2, yScroll + hScroll - 5, StaticObj.BOTTOM_HCENTER);
                }
            }
        }
    }
}
