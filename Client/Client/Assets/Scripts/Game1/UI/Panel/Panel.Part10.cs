namespace Game1
{
    using Game1.Assets.src.g;
    using Game1.God;
    using System;
    using System.Collections.Generic;
    using UnityEngine;
    
    public partial class Panel : IActionListener, IChatable
    {
    
        private void updateKeyInvenTab()
        {
            if (selected < 0)
            {
                return;
            }
            if (GameKeys.IsLeftPressed())
            {
                newSelected--;
                if (isnewInventory)
                {
                    currentListLength = 5;
                }
                if (newSelected < 0)
                {
                    newSelected = 0;
                    if (GameCanvas.isFocusPanel2)
                    {
                        GameCanvas.isFocusPanel2 = false;
                        GameCanvas.panel.selected = 0;
                    }
                }
            }
            else
            {
                if (!GameKeys.IsRightPressed())
                {
                    return;
                }
                newSelected++;
                if (isnewInventory)
                {
                    currentListLength = 5;
                }
                if (newSelected > size_tab - 1)
                {
                    newSelected = size_tab - 1;
                    if (GameCanvas.panel2 != null)
                    {
                        GameCanvas.isFocusPanel2 = true;
                        GameCanvas.panel2.selected = 0;
                    }
                }
            }
        }
    
        private void updateKeyInventory()
        {
            updateKeyScrollView();
            if (selected == 0)
            {
                updateKeyInvenTab();
            }
        }
    
        private bool IsTabOption()
        {
            if (type == 28 && currentTabIndex == 0 && selected >= 0 && selected < AutoSettingManager.getInstance().items.Count && AutoSettingManager.getInstance().items[selected].typeControl == 1)
            {
                return true;
            }
            if (size_tab > 0)
            {
                if (currentTabName.Length > 1)
                {
                    if (selected == 0)
                    {
                        return true;
                    }
                }
                else if (selected >= 0)
                {
                    return true;
                }
            }
            return false;
        }
    
        private int checkCurrentListLength(int arrLength)
        {
            int num = 20;
            int num2 = arrLength / 20 + ((arrLength % 20 > 0) ? 1 : 0);
            size_tab = (sbyte)num2;
            if (newSelected > num2 - 1)
            {
                newSelected = num2 - 1;
            }
            if (arrLength % 20 > 0 && newSelected == num2 - 1)
            {
                num = arrLength % 20;
            }
            return num + 1;
        }
    
        private void setNewSelected(int arrLength, bool resetSelect)
        {
            int num = arrLength / 20 + ((arrLength % 20 > 0) ? 1 : 0);
            int num2 = xScroll;
            newSelected = (GameCanvas.px - num2) / TAB_W_NEW;
            if (newSelected > num - 1)
            {
                newSelected = num - 1;
            }
            if (GameCanvas.px < num2)
            {
                newSelected = 0;
            }
            setTabInventory(resetSelect);
        }
    }
}
