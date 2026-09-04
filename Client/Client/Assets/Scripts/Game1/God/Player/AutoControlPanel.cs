using System.Collections;
using UnityEngine;

namespace Game1.God
{
    public class AutoControlPanel
    {
        private static AutoControlPanel instance;
        public bool isShow = false;
        private int xBtn, yBtn;
        private int panelW = 110;
        private int panelH = 140;
        
        public static AutoControlPanel getInstance()
        {
            if (instance == null) instance = new AutoControlPanel();
            return instance;
        }
        
        public void paint(mGraphics g)
        {
            try 
            {
                if (Char.myCharz() == null) return;

                xBtn = 135;
                yBtn = 20;
                
                // Draw Button using chat icon image to match aesthetics perfectly
                bool isHover = GameCanvas.isPointerHoldIn(xBtn - 15, yBtn - 15, 30, 30);
                
                if (GameScr.imgChat != null) {
                    g.drawImage(isHover && GameScr.imgChat2 != null ? GameScr.imgChat2 : GameScr.imgChat, xBtn, yBtn, mGraphics.HCENTER | mGraphics.VCENTER);
                } else {
                    g.setColor(isHover ? 0x2E7D32 : 0x4CAF50); // Fallback color
                    g.fillRect(xBtn - 15, yBtn - 10, 30, 20);
                }
                mFont.tahoma_7b_white.drawString(g, "Auto", xBtn, yBtn - 6, mFont.CENTER);
                
                // Draw Panel if expanded
                if (isShow)
                {
                    int xP = xBtn - 15;
                    int yP = yBtn + 15;
                    g.setColor(0, 0.7f); // Semi-transparent black
                    g.fillRect(xP, yP, panelW, panelH);
                    
                    // Draw items
                    drawToggle(g, "Tự Động Hồi Sinh", Revive.getInstance().getRevive(), xP, yP);
                    drawToggle(g, "Tự Đánh", nSkill.getInstance().canAttack, xP, yP + 15);
                    drawToggle(g, "Tàn Sát", Mobs.IsTanSat, xP, yP + 30);
                    drawToggle(g, "Auto Up Đệ", PetService.getInstance().getUp(), xP, yP + 45);
                    drawToggle(g, "Thông Báo BOSS", Boss.getInstance().isShow, xP, yP + 60);
                    drawToggle(g, "D.s Nhân Vật", ListChars.getInstance().isShow, xP, yP + 75);
                    drawToggle(g, "Auto Nhặt", Mobs.IsAutoPickItems, xP, yP + 90);
                    drawToggle(g, "Giảm Đồ Họa", ListChars.getInstance().HideMap, xP, yP + 105);
                    drawToggle(g, "Auto Login", PlayerInfo.getInstance().canLogin, xP, yP + 120);
                }
            } 
            catch (System.Exception) {}
            finally 
            {
                g.setColor(16777215, 1.0f); // Fully restore color and alpha
            }
        }
        
        private void drawToggle(mGraphics g, string name, bool isOn, int x, int y)
        {
            mFont.tahoma_7b_white.drawString(g, name, x + 5, y + 2, 0);
            
            // Draw switch
            int switchX = x + panelW - 25;
            int switchY = y + 2;
            g.setColor(isOn ? 0x4CAF50 : 0xF44336, 1.0f); // Green if ON, Red if OFF
            g.fillRect(switchX, switchY, 20, 10);
            mFont.tahoma_7b_white.drawString(g, isOn ? "ON" : "OFF", switchX + 10, switchY + 1, mFont.CENTER);
            g.setColor(0, 0.7f); // Restore panel background alpha
        }
        
        public void updateTouch()
        {
            try 
            {
                if (GameCanvas.isPointerClick)
                {
                    // Click Button
                    if (GameCanvas.isPointerHoldIn(xBtn - 15, yBtn - 15, 30, 30))
                    {
                        isShow = !isShow;
                        GameCanvas.isPointerClick = false;
                        return;
                    }
                    
                    // Click Items
                    if (isShow)
                    {
                        int xP = xBtn - 15;
                        int yP = yBtn + 15;
                        if (checkToggleClick(xP, yP)) { Revive.getInstance().setRevive(); return; }
                        if (checkToggleClick(xP, yP + 15)) { nSkill.getInstance().canAttack = !nSkill.getInstance().canAttack; return; }
                        if (checkToggleClick(xP, yP + 30)) { Mobs.IsTanSat = !Mobs.IsTanSat; return; }
                        if (checkToggleClick(xP, yP + 45)) { PetService.getInstance().setUp(); return; }
                        if (checkToggleClick(xP, yP + 60)) { Boss.getInstance().isShow = !Boss.getInstance().isShow; return; }
                        if (checkToggleClick(xP, yP + 75)) { ListChars.getInstance().isShow = !ListChars.getInstance().isShow; return; }
                        if (checkToggleClick(xP, yP + 90)) { Mobs.IsAutoPickItems = !Mobs.IsAutoPickItems; return; }
                        if (checkToggleClick(xP, yP + 105)) { ListChars.getInstance().HideMap = !ListChars.getInstance().HideMap; return; }
                        if (checkToggleClick(xP, yP + 120)) { PlayerInfo.getInstance().canLogin = !PlayerInfo.getInstance().canLogin; return; }
                    }
                }
            } 
            catch (System.Exception) {}
        }
        
        private bool checkToggleClick(int x, int y)
        {
            if (GameCanvas.isPointerHoldIn(x, y, panelW, 15))
            {
                GameCanvas.isPointerClick = false;
                return true;
            }
            return false;
        }
    }
}
