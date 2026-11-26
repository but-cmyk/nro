namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;

    public partial class Panel
    {
        public void paintClans(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(-cmx, -cmy);
            g.setColor(0);
            int num = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
            if (currentListLength == 2)
            {
                mFont.tahoma_7_green2.drawString(g, clanReport, xScroll + wScroll / 2, yScroll + 24 + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
                if (isMessage && myMember.size() == 1)
                {
                    for (int i = 0; i < mResources.clanEmpty.Length; i++)
                    {
                        mFont.tahoma_7b_dark.drawString(g, mResources.clanEmpty[i], xScroll + wScroll / 2, yScroll + 24 + hScroll / 2 - mResources.clanEmpty.Length * 12 / 2 + i * 12, mFont.CENTER);
                    }
                }
            }
            if (isMessage)
            {
                currentListLength = ClanMessage.vMessage.size() + 2;
            }
            for (int j = 0; j < currentListLength; j++)
            {
                int num2 = xScroll;
                int num3 = yScroll + j * ITEM_HEIGHT;
                int num4 = 24;
                int num5 = ITEM_HEIGHT - 1;
                int num6 = xScroll + num4;
                int num7 = yScroll + j * ITEM_HEIGHT;
                int num8 = wScroll - num4;
                int num9 = ITEM_HEIGHT - 1;
                if (num7 - cmy > yScroll + hScroll || num7 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                switch (j)
                {
                    case 0:
                        {
                            for (int k = 0; k < clansOption.Length; k++)
                            {
                                g.setColor((k != cSelected || j != selected) ? 15723751 : 16383818);
                                g.fillRect(num + k * TAB_W, num7, TAB_W - 1, 23);
                                for (int l = 0; l < clansOption[k].Length; l++)
                                {
                                    mFont.tahoma_7_grey.drawString(g, clansOption[k][l], num + k * TAB_W + TAB_W / 2, yScroll + l * 11, mFont.CENTER);
                                }
                            }
                            continue;
                        }
                    case 1:
                        g.setColor((j != selected) ? 15196114 : 16383818);
                        g.fillRect(xScroll, num7, wScroll, num9);
                        if (clanInfo != null)
                        {
                            mFont.tahoma_7b_dark.drawString(g, clanInfo, xScroll + wScroll / 2, num7 + 6, mFont.CENTER);
                        }
                        continue;
                }
                if (isSearchClan)
                {
                    if (clans == null || clans.Length == 0)
                    {
                        continue;
                    }
                    g.setColor((j != selected) ? 15196114 : 16383818);
                    g.fillRect(num6, num7, num8, num9);
                    g.setColor((j != selected) ? 9993045 : 9541120);
                    g.fillRect(num2, num3, num4, num5);
                    if (ClanImage.isExistClanImage(clans[j - 2].imgID))
                    {
                        if (ClanImage.getClanImage((short)clans[j - 2].imgID).idImage != null)
                        {
                            SmallImage.drawSmallImage(g, ClanImage.getClanImage((short)clans[j - 2].imgID).idImage[0], num2 + num4 / 2, num3 + num5 / 2, 0, StaticObj.VCENTER_HCENTER);
                        }
                    }
                    else
                    {
                        ClanImage clanImage = new ClanImage();
                        clanImage.ID = clans[j - 2].imgID;
                        if (!ClanImage.isExistClanImage(clanImage.ID))
                        {
                            ClanImage.addClanImage(clanImage);
                        }
                    }
                    string st = ((clans[j - 2].name.Length <= 23) ? clans[j - 2].name : (clans[j - 2].name.Substring(0, 23) + "..."));
                    mFont.tahoma_7b_green2.drawString(g, st, num6 + 5, num7, 0);
                    g.setClip(num6, num7, num8 - 10, num9);
                    mFont.tahoma_7_blue.drawString(g, clans[j - 2].slogan, num6 + 5, num7 + 11, 0);
                    g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                    mFont.tahoma_7_green2.drawString(g, clans[j - 2].currMember + "/" + clans[j - 2].maxMember, num6 + num8 - 5, num7, mFont.RIGHT);
                    continue;
                }
                if (isViewMember)
                {
                    g.setColor((j != selected) ? 15196114 : 16383818);
                    g.fillRect(num6, num7, num8, num9);
                    g.setColor((j != selected) ? 9993045 : 9541120);
                    g.fillRect(num2, num3, num4, num5);
                    Member member = ((this.member == null) ? ((Member)myMember.elementAt(j - 2)) : ((Member)this.member.elementAt(j - 2)));
                    if (member.headICON != -1)
                    {
                        SmallImage.drawSmallImage(g, member.headICON, num2, num3, 0, 0);
                    }
                    else
                    {
                        Part part = GameScr.parts[member.head];
                        SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num2 + part.pi[Char.CharInfo[0][0][0]].dx, num3 + 3 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                    }
                    g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                    mFont mFont2 = mFont.tahoma_7b_dark;
                    if (member.role == 0)
                    {
                        mFont2 = mFont.tahoma_7b_red;
                    }
                    else if (member.role == 1)
                    {
                        mFont2 = mFont.tahoma_7b_green;
                    }
                    else if (member.role == 2)
                    {
                        mFont2 = mFont.tahoma_7b_green2;
                    }
                    mFont2.drawString(g, member.name, num6 + 5, num7, 0);
                    mFont.tahoma_7_blue.drawString(g, mResources.power + ": " + member.powerPoint, num6 + 5, num7 + 11, 0);
                    SmallImage.drawSmallImage(g, 7223, num6 + num8 - 7, num7 + 12, 0, 3);
                    mFont.tahoma_7_blue.drawString(g, string.Empty + member.clanPoint, num6 + num8 - 15, num7 + 6, mFont.RIGHT);
                    continue;
                }
                if (!isMessage || ClanMessage.vMessage.size() == 0)
                {
                    continue;
                }
                ClanMessage clanMessage = (ClanMessage)ClanMessage.vMessage.elementAt(j - 2);
                g.setColor((j != selected || clanMessage.option != null) ? 15196114 : 16383818);
                g.fillRect(num2, num3, num8 + num4, num9);
                clanMessage.paint(g, num2, num3);
                if (clanMessage.option == null)
                {
                    continue;
                }
                int num10 = xScroll + wScroll - 2 - clanMessage.option.Length * 40;
                for (int m = 0; m < clanMessage.option.Length; m++)
                {
                    if (m == cSelected && j == selected)
                    {
                        g.drawImage(GameScr.imgLbtnFocus2, num10 + m * 40 + 20, num7 + num9 / 2, StaticObj.VCENTER_HCENTER);
                        mFont.tahoma_7b_green2.drawString(g, clanMessage.option[m], num10 + m * 40 + 20, num7 + 6, mFont.CENTER);
                    }
                    else
                    {
                        g.drawImage(GameScr.imgLbtn2, num10 + m * 40 + 20, num7 + num9 / 2, StaticObj.VCENTER_HCENTER);
                        mFont.tahoma_7b_dark.drawString(g, clanMessage.option[m], num10 + m * 40 + 20, num7 + 6, mFont.CENTER);
                    }
                }
            }
            paintScrollArrow(g);
        }
    }
}
